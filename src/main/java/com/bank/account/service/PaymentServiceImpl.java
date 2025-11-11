package com.bank.account.service;

import com.bank.account.dto.FundTransferRequest;
import com.bank.account.dto.TransferType;
import com.bank.account.entity.Account;
import com.bank.account.entity.Transaction;
import com.bank.account.exception.InvalidInputException;
import com.bank.account.exception.InvalidTransferAmountException;
import com.bank.account.exception.ResourceNotFoundException;
import com.bank.account.repository.AccountRepository;
import com.bank.account.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository; // Needed to get account details for transaction creation

    @Autowired
    private TransactionRepository transactionRepository;

    private static final double NEFT_FEE = 2.50;
    private static final double RTGS_FEE = 25.00;
    private static final double IMPS_FEE = 5.00;

    private static final double RTGS_MIN_AMOUNT = 200000.00;
    private static final double IMPS_MAX_AMOUNT = 500000.00;

    @Override
    @Transactional
    public List<Transaction> initiateFundTransfer(FundTransferRequest request) {
        List<Transaction> generatedTransactions = new ArrayList<>();

        // Common Checks
        if (request.getAmount() <= 0) {
            throw new InvalidInputException("Transfer amount must be positive.");
        }

        Account fromAccount = accountService.getAccountDetails(request.getFromAccountNumber());
        Account toAccount = accountService.getAccountDetails(request.getToAccountNumber());

        if (fromAccount.getAccountNumber().equals(toAccount.getAccountNumber())) {
            throw new InvalidInputException("Cannot transfer funds to the same account.");
        }

        double transferAmount = request.getAmount();
        double fee = 0;
        String transactionType = request.getTransferType().name();

        switch (request.getTransferType()) {
            case NEFT:
                fee = NEFT_FEE;
                // NEFT specific validation (no max limit, min 1 handled by common check)
                break;
            case RTGS:
                fee = RTGS_FEE;
                if (transferAmount < RTGS_MIN_AMOUNT) {
                    throw new InvalidTransferAmountException("RTGS minimum transfer amount is " + RTGS_MIN_AMOUNT);
                }
                break;
            case IMPS:
                fee = IMPS_FEE;
                if (transferAmount > IMPS_MAX_AMOUNT) {
                    throw new InvalidTransferAmountException("IMPS maximum transfer amount is " + IMPS_MAX_AMOUNT);
                }
                break;
            default:
                throw new InvalidInputException("Invalid transfer type: " + request.getTransferType());
        }

        double totalDebitAmount = transferAmount + fee;

        // Perform withdrawal from sender's account (includes fee)
        accountService.withdraw(fromAccount.getAccountNumber(), totalDebitAmount);
        generatedTransactions.add(createTransaction(fromAccount, -totalDebitAmount, transactionType + "_DEBIT", "Transfer to " + toAccount.getAccountNumber() + " (" + request.getRemarks() + ")"));

        // Record fee transaction
        if (fee > 0) {
            generatedTransactions.add(createTransaction(fromAccount, -fee, "FEE", request.getTransferType().name() + " Transfer Fee"));
        }

        // Perform deposit to receiver's account
        accountService.deposit(toAccount.getAccountNumber(), transferAmount);
        generatedTransactions.add(createTransaction(toAccount, transferAmount, transactionType + "_CREDIT", "Transfer from " + fromAccount.getAccountNumber() + " (" + request.getRemarks() + ")"));

        // Simulate NEFT delay (optional)
        if (request.getTransferType() == TransferType.NEFT) {
            try {
                Thread.sleep(3600000); // 1 hour delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("NEFT transfer interrupted", e); // Keep as RuntimeException for unexpected system issues
            }
        }

        transactionRepository.saveAll(generatedTransactions);
        return generatedTransactions;
    }

    private Transaction createTransaction(Account account, double amount, String type, String description) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setAccount(account);
        transaction.setAmount(Math.abs(amount)); // Store absolute amount
        transaction.setTransactionType(type);
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription(description);
        return transaction;
    }
}
