package com.bank.account.service;

import com.bank.account.dto.DepositRequest;
import com.bank.account.dto.TransferRequest;
import com.bank.account.dto.WithdrawRequest;
import com.bank.account.entity.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionService {

    Transaction deposit(DepositRequest request);

    Transaction withdraw(WithdrawRequest request);

    List<Transaction> transfer(TransferRequest request);

    List<Transaction> getAllTransactions();

    Optional<Transaction> getTransactionById(Long id);

    List<Transaction> getTransactionsForAccount(String accountNumber);
}
