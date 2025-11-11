package com.bank.account.service;

import com.bank.account.dto.FundTransferRequest;
import com.bank.account.entity.Transaction;

import java.util.List;

public interface PaymentService {
    List<Transaction> initiateFundTransfer(FundTransferRequest request);
}
