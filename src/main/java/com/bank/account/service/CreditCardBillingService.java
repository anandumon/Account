package com.bank.account.service;

import com.bank.account.dto.PaymentOption;
import com.bank.account.entity.CreditCardBill;

import java.time.LocalDate;
import java.util.List;

public interface CreditCardBillingService {

    CreditCardBill generateMonthlyStatement(String cardNumber);
    CreditCardBill generateBill(String cardNumber);
    CreditCardBill getBillDetails(Long billId);
    List<CreditCardBill> getBillingHistory(String cardNumber);
    CreditCardBill payBill(String cardNumber, PaymentOption paymentOption, double amount);
    void changeBillGenerationDate(String cardNumber, int dayOfMonth);
}
