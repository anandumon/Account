package com.bank.account.service;

import com.bank.account.dto.AccountStatus;
import com.bank.account.dto.DepositRequest;
import com.bank.account.dto.TransferRequest;
import com.bank.account.dto.WithdrawRequest;
import com.bank.account.entity.Account;
import com.bank.account.entity.Transaction;
import com.bank.account.repository.AccountRepository;
import com.bank.account.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    @Transactional
    public Transaction deposit(DepositRequest request) {
        Account account = getAccount(request.getAccountNumber());
        checkIfFrozen(account);
        account.setBalance(account.getBalance() + request.getAmount());

        Transaction transaction = createTransaction(account, request.getAmount(), "DEPOSIT", "Deposit of " + request.getAmount());
        accountRepository.save(account);
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction withdraw(WithdrawRequest request) {
        Account account = getAccount(request.getAccountNumber());
        checkIfFrozen(account);
        if (account.getBalance() < request.getAmount()) {
            throw new RuntimeException("Insufficient funds");
        }
        account.setBalance(account.getBalance() - request.getAmount());

        Transaction transaction = createTransaction(account, request.getAmount(), "WITHDRAWAL", "Withdrawal of " + request.getAmount());
        accountRepository.save(account);
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public List<Transaction> transfer(TransferRequest request) {
        Account fromAccount = getAccount(request.getFromAccountNumber());
        Account toAccount = getAccount(request.getToAccountNumber());

        checkIfFrozen(fromAccount);
        checkIfFrozen(toAccount);

        if (fromAccount.getBalance() < request.getAmount()) {
            throw new RuntimeException("Insufficient funds");
        }

        fromAccount.setBalance(fromAccount.getBalance() - request.getAmount());
        toAccount.setBalance(toAccount.getBalance() + request.getAmount());

        Transaction fromTransaction = createTransaction(fromAccount, request.getAmount(), "TRANSFER", "Transfer to " + request.getToAccountNumber());
        Transaction toTransaction = createTransaction(toAccount, request.getAmount(), "TRANSFER", "Transfer from " + request.getFromAccountNumber());

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        return transactionRepository.saveAll(List.of(fromTransaction, toTransaction));
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    @Override
    public List<Transaction> getTransactionsForAccount(String accountNumber) {
        return transactionRepository.findByAccount_AccountNumber(accountNumber);
    }

    private Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
    }

    private void checkIfFrozen(Account account) {
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new RuntimeException("Account is frozen: " + account.getAccountNumber());
        }
    }

    private Transaction createTransaction(Account account, double amount, String type, String description) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setAccount(account);
        transaction.setAmount(amount);
        transaction.setTransactionType(type);
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription(description);
        return transaction;
    }
}
