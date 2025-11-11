package com.bank.account.controller;

import com.bank.account.dto.ChangeBillGenerationDateRequest;
import com.bank.account.dto.PayBillRequest;
import com.bank.account.entity.CreditCardBill;
import com.bank.account.service.CreditCardBillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credit-card-bills")
public class CreditCardBillingController {

    @Autowired
    private CreditCardBillingService creditCardBillingService;

    @GetMapping("/{cardNumber}/statement")
    public CreditCardBill getMonthlyStatement(@PathVariable String cardNumber) {
        return creditCardBillingService.generateMonthlyStatement(cardNumber);
    }

    @PostMapping("/{cardNumber}/generate-bill")
    public CreditCardBill generateBill(@PathVariable String cardNumber) {
        return creditCardBillingService.generateBill(cardNumber);
    }

    @GetMapping("/bill/{billId}")
    public CreditCardBill getBillDetails(@PathVariable Long billId) {
        return creditCardBillingService.getBillDetails(billId);
    }

    @GetMapping("/{cardNumber}/history")
    public List<CreditCardBill> getBillingHistory(@PathVariable String cardNumber) {
        return creditCardBillingService.getBillingHistory(cardNumber);
    }

    @PostMapping("/{cardNumber}/pay")
    public CreditCardBill payBill(@PathVariable String cardNumber, @RequestBody PayBillRequest request) {
        return creditCardBillingService.payBill(cardNumber, request.getPaymentOption(), request.getAmount());
    }

    @PatchMapping("/{cardNumber}/bill-generation-date")
    public ResponseEntity<Void> changeBillGenerationDate(@PathVariable String cardNumber, @RequestBody ChangeBillGenerationDateRequest request) {
        creditCardBillingService.changeBillGenerationDate(cardNumber, request.getDayOfMonth());
        return ResponseEntity.noContent().build();
    }
}
