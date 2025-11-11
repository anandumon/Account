package com.bank.account.controller;

import com.bank.account.dto.AccountCreationRequest;
import com.bank.account.dto.AccountStatus;
import com.bank.account.entity.Account;
import com.bank.account.entity.Transaction;
import com.bank.account.service.AccountService;
import com.bank.account.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public Account createAccount(@Valid @RequestBody AccountCreationRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{accountNumber}")
    public Account getAccountDetails(@PathVariable String accountNumber) {
        return accountService.getAccountDetails(accountNumber);
    }

    @PutMapping("/{accountNumber}")
    public Account updateAccount(@PathVariable String accountNumber, @Valid @RequestBody Account accountDetails) {
        return accountService.updateAccount(accountNumber, accountDetails);
    }

    @PatchMapping("/{accountNumber}/status")
    public Account updateAccountStatus(@PathVariable String accountNumber, @RequestBody Map<String, String> request) {
        // Enum validation will happen in service layer
        AccountStatus status = AccountStatus.valueOf(request.get("status").toUpperCase());
        return accountService.updateAccountStatus(accountNumber, status);
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<Double> getBalance(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getBalance(accountNumber));
    }

    @GetMapping("/{accountNumber}/transactions")
    public List<Transaction> getTransactionsForAccount(@PathVariable String accountNumber) {
        return transactionService.getTransactionsForAccount(accountNumber);
    }
}
