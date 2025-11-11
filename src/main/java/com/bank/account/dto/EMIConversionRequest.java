package com.bank.account.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EMIConversionRequest {
    @NotNull(message = "Transaction ID cannot be null")
    private Long transactionId;

    @Min(value = 1, message = "Tenure must be at least 1 month")
    private int tenureMonths; // Further validation for specific tenures in service
}
