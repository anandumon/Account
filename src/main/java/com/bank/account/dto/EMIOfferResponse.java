package com.bank.account.dto;

import lombok.Data;

import java.util.List;

@Data
public class EMIOfferResponse {
    private Long transactionId;
    private double originalAmount;
    private double processingFee;
    private List<EMIOption> emiOptions;

    @Data
    public static class EMIOption {
        private int tenureMonths;
        private double interestRate;
        private double monthlyInstallment;
        private double totalPayable;
    }
}
