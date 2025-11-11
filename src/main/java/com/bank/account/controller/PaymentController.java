package com.bank.account.controller;

import com.bank.account.dto.FundTransferRequest;
import com.bank.account.entity.Transaction;
import com.bank.account.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/transfer")
    public List<Transaction> initiateTransfer(@RequestBody FundTransferRequest request) {
        return paymentService.initiateFundTransfer(request);
    }
}
