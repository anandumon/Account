package com.bank.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FundTransferRequest {
    @NotBlank(message = "From account number cannot be empty")
    private String fromAccountNumber;

    @NotBlank(message = "To account number cannot be empty")
    private String toAccountNumber;

    @DecimalMin(value = "0.0", inclusive = false, message = "Transfer amount must be positive")
    private double amount;

    @NotNull(message = "Transfer type cannot be null")
    private TransferType transferType;

    private String remarks;
}
