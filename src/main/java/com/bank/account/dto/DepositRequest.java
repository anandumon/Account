package com.bank.account.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepositRequest {
    @NotBlank(message = "Account number cannot be empty")
    private String accountNumber;

    @Min(value = 0, inclusive = false, message = "Deposit amount must be positive")
    private double amount;
}
