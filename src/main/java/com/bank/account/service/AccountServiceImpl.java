package com.bank.account.service;

import com.bank.account.dto.AccountCreationRequest;
import com.bank.account.dto.AccountStatus;
import com.bank.account.entity.Account;
import com.bank.account.entity.Customer;
import com.bank.account.entity.Transaction;
import com.bank.account.exception.AccountFrozenException;
import com.bank.account.exception.InsufficientFundsException;
import com.bank.account.exception.ResourceNotFoundException;
import com.bank.account.repository.AccountRepository;
import com.bank.account.repository.CustomerRepository;
import com.bank.account.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public Account createAccount(AccountCreationRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId()));
        Account account = new Account();
        account.setCustomer(customer);
        account.setAccountNumber(request.getAccountNumber());
        account.setAccountType(request.getAccountType());
        account.setIfscCode(request.getIfscCode());
        account.setBranch(request.getBranch());
        account.setBalance(request.getBalance());
        return accountRepository.save(account);
    }

    @Override
    public Account getAccountDetails(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with number: " + accountNumber));
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public Account updateAccount(String accountNumber, Account accountDetails) {
        Account account = getAccountDetails(accountNumber);
        account.setAccountType(accountDetails.getAccountType());
        account.setBranch(accountDetails.getBranch());
        account.setIfscCode(accountDetails.getIfscCode());
        return accountRepository.save(account);
    }

    @Override
    public Account updateAccountStatus(String accountNumber, AccountStatus status) {
        Account account = getAccountDetails(accountNumber);
        account.setStatus(status);
        return accountRepository.save(account);
    }

    @Override
    public double getBalance(String accountNumber) {
        return getAccountDetails(accountNumber).getBalance();
    }

    @Override
    @Transactional
    public Account deposit(String accountNumber, double amount) {
        Account account = getAccountDetails(accountNumber);
        checkIfFrozen(account);
        account.setBalance(account.getBalance() + amount);

        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setAccount(account);
        transaction.setAmount(amount);
        transaction.setTransactionType("DEPOSIT");
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Deposit of " + amount);
        transactionRepository.save(transaction);

        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public Account withdraw(String accountNumber, double amount) {
        Account account = getAccountDetails(accountNumber);
        checkIfFrozen(account);
        if (account.getBalance() < amount) {
            throw new InsufficientFundsException("Insufficient funds in account: " + accountNumber);
        }
        account.setBalance(account.getBalance() - amount);

        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setAccount(account);
        transaction.setAmount(amount);
        transaction.setTransactionType("WITHDRAWAL");
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Withdrawal of " + amount);
        transactionRepository.save(transaction);

        return accountRepository.save(account);
    }

    private void checkIfFrozen(Account account) {
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new AccountFrozenException("Account is frozen: " + account.getAccountNumber());
        }
    }
}
