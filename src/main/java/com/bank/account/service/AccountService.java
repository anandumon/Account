package com.bank.account.service;

import com.bank.account.dto.AccountCreationRequest;
import com.bank.account.dto.AccountStatus;
import com.bank.account.entity.Account;

import java.util.List;

public interface AccountService {

    Account createAccount(AccountCreationRequest request);

    Account getAccountDetails(String accountNumber);

    List<Account> getAllAccounts();

    Account updateAccount(String accountNumber, Account accountDetails);

    Account updateAccountStatus(String accountNumber, AccountStatus status);

    double getBalance(String accountNumber);

    Account deposit(String accountNumber, double amount);

    Account withdraw(String accountNumber, double amount);
}
