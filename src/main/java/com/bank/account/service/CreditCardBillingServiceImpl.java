package com.bank.account.service;

import com.bank.account.dto.CardType;
import com.bank.account.dto.PaymentOption;
import com.bank.account.dto.PaymentStatus;
import com.bank.account.entity.Card;
import com.bank.account.entity.CreditCardBill;
import com.bank.account.entity.Transaction;
import com.bank.account.exception.InsufficientFundsException;
import com.bank.account.exception.InvalidInputException;
import com.bank.account.exception.ResourceNotFoundException;
import com.bank.account.repository.CardRepository;
import com.bank.account.repository.CreditCardBillRepository;
import com.bank.account.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreditCardBillingServiceImpl implements CreditCardBillingService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CreditCardBillRepository creditCardBillRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountService accountService; // To interact with account for payments

    private static final int PAYMENT_DUE_DAYS = 15; // Pay before 15 days from bill generation date

    @Override
    public CreditCardBill generateMonthlyStatement(String cardNumber) {
        Card card = getCreditCard(cardNumber);
        LocalDate today = LocalDate.now();
        LocalDate billingDate = today.withDayOfMonth(card.getBillGenerationDay());

        // If today is before the billing day, generate for previous month
        if (today.getDayOfMonth() < card.getBillGenerationDay()) {
            billingDate = billingDate.minusMonths(1);
        }

        // Check if a bill for this billing cycle already exists
        return creditCardBillRepository.findByCard_CardNumberAndBillingDate(cardNumber, billingDate)
                .orElseGet(() -> generateBill(cardNumber)); // Generate if not exists
    }

    @Override
    @Transactional
    public CreditCardBill generateBill(String cardNumber) {
        Card card = getCreditCard(cardNumber);
        LocalDate today = LocalDate.now();
        LocalDate billingDate = today.withDayOfMonth(card.getBillGenerationDay());

        // If today is before the billing day, generate for previous month
        if (today.getDayOfMonth() < card.getBillGenerationDay()) {
            billingDate = billingDate.minusMonths(1);
        }

        // Define the statement period (e.g., from previous billing date + 1 day to current billing date)
        LocalDate previousBillingDate = billingDate.minusMonths(1).withDayOfMonth(card.getBillGenerationDay());
        LocalDate statementStartDate = previousBillingDate.plusDays(1);
        LocalDate statementEndDate = billingDate;

        List<Transaction> cardTransactions = transactionRepository.findByAccount_AccountNumber(card.getAccount().getAccountNumber())
                .stream()
                .filter(t -> t.getDate().toLocalDate().isAfter(statementStartDate.minusDays(1)) &&
                             t.getDate().toLocalDate().isBefore(statementEndDate.plusDays(1)))
                .filter(t -> t.getDescription().contains(cardNumber)) // Assuming card transactions are marked
                .collect(Collectors.toList());

        double totalAmountDue = cardTransactions.stream()
                .filter(t -> t.getTransactionType().equals("DEBIT") || t.getTransactionType().equals("TRANSFER")) // Assuming these are charges
                .mapToDouble(Transaction::getAmount)
                .sum();

        // Payments made during the cycle reduce the outstanding
        double paymentsMade = cardTransactions.stream()
                .filter(t -> t.getTransactionType().equals("CREDIT")) // Assuming these are payments
                .mapToDouble(Transaction::getAmount)
                .sum();

        totalAmountDue -= paymentsMade;
        double minimumAmountDue = calculateMinimumAmountDue(totalAmountDue); // Implement your logic for min amount
        double currentOutstanding = totalAmountDue; // At bill generation, outstanding is total due

        CreditCardBill bill = new CreditCardBill();
        bill.setCard(card);
        bill.setBillingDate(billingDate);
        bill.setDueDate(billingDate.plusDays(PAYMENT_DUE_DAYS));
        bill.setTotalAmountDue(totalAmountDue);
        bill.setMinimumAmountDue(minimumAmountDue);
        bill.setCurrentOutstanding(currentOutstanding);
        bill.setPaymentStatus(PaymentStatus.UNPAID);

        // Update card's currentCreditUsed
        card.setCurrentCreditUsed(currentOutstanding);
        cardRepository.save(card);

        return creditCardBillRepository.save(bill);
    }

    @Override
    public CreditCardBill getBillDetails(Long billId) {
        return creditCardBillRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit Card Bill not found with ID: " + billId));
    }

    @Override
    public List<CreditCardBill> getBillingHistory(String cardNumber) {
        return creditCardBillRepository.findByCard_CardNumberOrderByBillingDateDesc(cardNumber);
    }

    @Override
    @Transactional
    public CreditCardBill payBill(String cardNumber, PaymentOption paymentOption, double amount) {
        Card card = getCreditCard(cardNumber);

        // Find the latest unpaid or partially paid bill
        CreditCardBill latestBill = creditCardBillRepository.findTopByCard_CardNumberAndPaymentStatusNotOrderByBillingDateDesc(cardNumber, PaymentStatus.PAID)
                .orElseThrow(() -> new ResourceNotFoundException("No outstanding bill found for card: " + cardNumber));

        double amountToPay = 0;
        switch (paymentOption) {
            case FULL_AMOUNT:
                amountToPay = latestBill.getCurrentOutstanding();
                break;
            case MINIMUM_AMOUNT:
                amountToPay = latestBill.getMinimumAmountDue();
                break;
            case CURRENT_OUTSTANDING: // This option is for before bill generation
                amountToPay = card.getCurrentCreditUsed(); // Amount currently used on the card
                break;
            case OTHER_AMOUNT:
                amountToPay = amount;
                break;
        }

        if (amountToPay <= 0) {
            throw new InvalidInputException("Payment amount must be positive.");
        }

        // Perform the payment from the linked account
        // This will throw InsufficientFundsException if balance is low
        accountService.withdraw(card.getAccount().getAccountNumber(), amountToPay);

        // Record the payment as a transaction
        // This is a payment to the card, so it's a credit to the card's balance
        // The description should reflect this is a bill payment
        accountService.deposit(card.getAccount().getAccountNumber(), amountToPay);

        // Update the bill status
        latestBill.setCurrentOutstanding(latestBill.getCurrentOutstanding() - amountToPay);
        if (latestBill.getCurrentOutstanding() <= 0) {
            latestBill.setPaymentStatus(PaymentStatus.PAID);
            latestBill.setCurrentOutstanding(0);
        } else {
            latestBill.setPaymentStatus(PaymentStatus.PARTIAL);
        }

        // Update card's currentCreditUsed
        card.setCurrentCreditUsed(card.getCurrentCreditUsed() - amountToPay);
        cardRepository.save(card);

        return creditCardBillRepository.save(latestBill);
    }

    @Override
    @Transactional
    public void changeBillGenerationDate(String cardNumber, int dayOfMonth) {
        if (dayOfMonth < 1 || dayOfMonth > 31) {
            throw new InvalidInputException("Bill generation day must be between 1 and 31.");
        }
        Card card = getCreditCard(cardNumber);
        card.setBillGenerationDay(dayOfMonth);
        cardRepository.save(card);
    }

    private Card getCreditCard(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with number: " + cardNumber));
        if (card.getCardType() != CardType.CREDIT) {
            throw new InvalidInputException("Card is not a credit card: " + cardNumber);
        }
        return card;
    }

    private double calculateMinimumAmountDue(double totalAmountDue) {
        // Example: 5% of total amount due, or a flat fee, whichever is higher
        return Math.max(totalAmountDue * 0.05, 50.00); // Minimum $50 or 5%
    }
}
