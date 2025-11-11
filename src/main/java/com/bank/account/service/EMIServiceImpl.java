package com.bank.account.service;

import com.bank.account.dto.CardType;
import com.bank.account.dto.EMIConversionRequest;
import com.bank.account.dto.EMIOfferResponse;
import com.bank.account.entity.*;
import com.bank.account.exception.EmiConversionException;
import com.bank.account.exception.InvalidInputException;
import com.bank.account.exception.ResourceNotFoundException;
import com.bank.account.repository.EMIPlanRepository;
import com.bank.account.repository.EMIScheduleRepository;
import com.bank.account.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EMIServiceImpl implements EMIService {

    private static final double INTEREST_RATE = 0.16; // 16% annual interest
    private static final double PROCESSING_FEE_PERCENTAGE = 0.025; // 2.5% processing fee
    private static final double MIN_TRANSACTION_AMOUNT_FOR_EMI = 1500.00;
    private static final int[] ALLOWED_TENURES = {3, 6, 9, 12, 18, 24}; // Example tenures

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EMIPlanRepository emiPlanRepository;

    @Autowired
    private EMIScheduleRepository emiScheduleRepository;

    @Autowired
    private AccountService accountService; // To process EMI payments

    @Override
    public EMIOfferResponse getEmiOffers(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + transactionId));

        if (transaction.getAmount() < MIN_TRANSACTION_AMOUNT_FOR_EMI) {
            throw new EmiConversionException("Transaction amount (" + transaction.getAmount() + ") is below the minimum (" + MIN_TRANSACTION_AMOUNT_FOR_EMI + ") for EMI conversion.");
        }
        if (transaction.isEmiConverted()) {
            throw new EmiConversionException("Transaction already converted to EMI.");
        }

        EMIOfferResponse response = new EMIOfferResponse();
        response.setTransactionId(transactionId);
        response.setOriginalAmount(transaction.getAmount());

        double processingFee = transaction.getAmount() * PROCESSING_FEE_PERCENTAGE;
        response.setProcessingFee(processingFee);

        List<EMIOfferResponse.EMIOption> emiOptions = new ArrayList<>();

        for (int tenure : ALLOWED_TENURES) {
            double principal = transaction.getAmount() + processingFee;
            double monthlyInterestRate = INTEREST_RATE / 12;
            double monthlyInstallment = (principal * monthlyInterestRate) /
                                        (1 - Math.pow(1 + monthlyInterestRate, -tenure));
            double totalPayable = monthlyInstallment * tenure;

            EMIOfferResponse.EMIOption option = new EMIOfferResponse.EMIOption();
            option.setTenureMonths(tenure);
            option.setInterestRate(INTEREST_RATE * 100); // Display as percentage
            option.setMonthlyInstallment(monthlyInstallment);
            option.setTotalPayable(totalPayable);
            emiOptions.add(option);
        }
        response.setEmiOptions(emiOptions);
        return response;
    }

    @Override
    @Transactional
    public EMIPlan convertToEmi(EMIConversionRequest request) {
        Transaction originalTransaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Original transaction not found with ID: " + request.getTransactionId()));

        if (originalTransaction.getAmount() < MIN_TRANSACTION_AMOUNT_FOR_EMI) {
            throw new EmiConversionException("Transaction amount (" + originalTransaction.getAmount() + ") is below the minimum (" + MIN_TRANSACTION_AMOUNT_FOR_EMI + ") for EMI conversion.");
        }
        if (originalTransaction.isEmiConverted()) {
            throw new EmiConversionException("Transaction already converted to EMI.");
        }
        if (!Arrays.stream(ALLOWED_TENURES).anyMatch(t -> t == request.getTenureMonths())) {
            throw new InvalidInputException("Invalid EMI tenure. Allowed tenures are: " + Arrays.toString(ALLOWED_TENURES));
        }

        Card card = originalTransaction.getAccount().getCustomer().getAccounts().stream()
                .flatMap(account -> account.getCards().stream())
                .filter(c -> c.getCardType() == CardType.CREDIT)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Credit card not found for the account linked to transaction ID: " + request.getTransactionId()));

        double processingFee = originalTransaction.getAmount() * PROCESSING_FEE_PERCENTAGE;
        double principalAmountForEmi = originalTransaction.getAmount() + processingFee;
        double monthlyInterestRate = INTEREST_RATE / 12;

        double monthlyInstallment = (principalAmountForEmi * monthlyInterestRate) /
                                    (1 - Math.pow(1 + monthlyInterestRate, -request.getTenureMonths()));
        double totalPayableAmount = monthlyInstallment * request.getTenureMonths();

        // Create EMI Plan
        EMIPlan emiPlan = new EMIPlan();
        emiPlan.setOriginalTransaction(originalTransaction);
        emiPlan.setCard(card);
        emiPlan.setOriginalAmount(originalTransaction.getAmount());
        emiPlan.setTenureMonths(request.getTenureMonths());
        emiPlan.setInterestRate(INTEREST_RATE);
        emiPlan.setProcessingFeePercentage(PROCESSING_FEE_PERCENTAGE);
        emiPlan.setTotalPayableAmount(totalPayableAmount);
        emiPlan.setMonthlyInstallmentAmount(monthlyInstallment);
        emiPlan.setStartDate(LocalDate.now());
        emiPlan.setEndDate(LocalDate.now().plusMonths(request.getTenureMonths()));
        emiPlan.setStatus("ACTIVE");

        // Generate EMI Schedule
        List<EMISchedule> emiSchedules = new ArrayList<>();
        double remainingPrincipal = principalAmountForEmi;
        for (int i = 1; i <= request.getTenureMonths(); i++) {
            double interestComponent = remainingPrincipal * monthlyInterestRate;
            double principalComponent = monthlyInstallment - interestComponent;

            EMISchedule schedule = new EMISchedule();
            schedule.setEmiPlan(emiPlan);
            schedule.setInstallmentNumber(i);
            schedule.setPrincipalAmount(principalComponent);
            schedule.setInterestAmount(interestComponent);
            schedule.setTotalInstallmentAmount(monthlyInstallment);
            schedule.setDueDate(LocalDate.now().plusMonths(i));
            schedule.setStatus("PENDING");
            emiSchedules.add(schedule);

            remainingPrincipal -= principalComponent;
        }
        emiPlan.setEmiSchedule(emiSchedules);

        // Mark original transaction as EMI converted
        originalTransaction.setEmiConverted(true);
        transactionRepository.save(originalTransaction);

        return emiPlanRepository.save(emiPlan);
    }

    @Override
    public EMIPlan getEmiPlanDetails(Long emiPlanId) {
        return emiPlanRepository.findById(emiPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("EMI Plan not found with ID: " + emiPlanId));
    }

    @Override
    public List<EMISchedule> getEmiSchedule(Long emiPlanId) {
        return emiScheduleRepository.findByEmiPlan_IdOrderByInstallmentNumberAsc(emiPlanId);
    }

    @Override
    @Transactional
    public void processMonthlyEmiInstallments(LocalDate date) {
        List<EMISchedule> pendingInstallments = emiScheduleRepository.findAll().stream()
                .filter(schedule -> schedule.getStatus().equals("PENDING") && schedule.getDueDate().isEqual(date))
                .collect(Collectors.toList());

        for (EMISchedule schedule : pendingInstallments) {
            try {
                // Deduct EMI amount from the linked account
                accountService.withdraw(schedule.getEmiPlan().getCard().getAccount().getAccountNumber(), schedule.getTotalInstallmentAmount());

                // Record as a transaction
                Transaction emiPaymentTransaction = new Transaction();
                emiPaymentTransaction.setAccount(schedule.getEmiPlan().getCard().getAccount());
                emiPaymentTransaction.setAmount(schedule.getTotalInstallmentAmount());
                emiPaymentTransaction.setTransactionType("EMI_PAYMENT");
                emiPaymentTransaction.setDate(LocalDateTime.now());
                emiPaymentTransaction.setDescription("EMI Payment for plan " + schedule.getEmiPlan().getId() + ", Installment " + schedule.getInstallmentNumber());
                transactionRepository.save(emiPaymentTransaction);

                schedule.setStatus("PAID");
                emiScheduleRepository.save(schedule);

                // Check if EMI plan is completed
                long remainingPending = emiScheduleRepository.findByEmiPlan_IdOrderByInstallmentNumberAsc(schedule.getEmiPlan().getId())
                        .stream()
                        .filter(s -> s.getStatus().equals("PENDING"))
                        .count();

                if (remainingPending == 0) {
                    EMIPlan emiPlan = schedule.getEmiPlan();
                    emiPlan.setStatus("COMPLETED");
                    emiPlanRepository.save(emiPlan);
                }
            } catch (Exception e) {
                // Handle cases where payment fails (e.g., insufficient funds)
                // Mark as OVERDUE, send notification, etc.
                schedule.setStatus("OVERDUE");
                emiScheduleRepository.save(schedule);
                System.err.println("Failed to process EMI installment " + schedule.getId() + ": " + e.getMessage());
            }
        }
    }
}
