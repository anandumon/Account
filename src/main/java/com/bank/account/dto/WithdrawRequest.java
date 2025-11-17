package com.bank.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WithdrawRequest {
    @NotBlank(message = "Account number cannot be empty")
    private String accountNumber;

    @DecimalMin(value = "0.0", inclusive = false, message = "Withdrawal amount must be positive")
    private double amount;
}
