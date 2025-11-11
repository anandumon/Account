package com.bank.account.controller;

import com.bank.account.dto.DepositRequest;
import com.bank.account.dto.FundTransferRequest;
import com.bank.account.dto.WithdrawRequest;
import com.bank.account.entity.Transaction;
import com.bank.account.exception.ResourceNotFoundException;
import com.bank.account.service.TransactionService;
import com.bank.account.service.PaymentService; // Inject PaymentService for transfer
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private PaymentService paymentService; // Inject PaymentService for handling transfers

    @PostMapping("/deposit")
    public Transaction deposit(@Valid @RequestBody DepositRequest request) {
        return transactionService.deposit(request);
    }

    @PostMapping("/withdraw")
    public Transaction withdraw(@Valid @RequestBody WithdrawRequest request) {
        return transactionService.withdraw(request);
    }

    @PostMapping("/transfer") // As per user's request
    public List<Transaction> transfer(@Valid @RequestBody FundTransferRequest request) {
        return paymentService.initiateFundTransfer(request);
    }

    @GetMapping
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/{id}")
    public Transaction getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));
    }
}
