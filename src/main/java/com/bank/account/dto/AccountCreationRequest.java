package com.bank.account.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AccountCreationRequest {
    @NotNull(message = "Customer ID cannot be null")
    private Long customerId;

    @NotBlank(message = "Account number cannot be empty")
    @Pattern(regexp = "^[0-9]{10,18}$", message = "Account number must be 10 to 18 digits")
    private String accountNumber;

    @NotBlank(message = "Account type cannot be empty")
    private String accountType; // e.g., SAVINGS, CURRENT - further validation in service

    @NotBlank(message = "IFSC Code cannot be empty")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC Code format")
    private String ifscCode;

    @NotBlank(message = "Branch cannot be empty")
    private String branch;

    @Min(value = 0, message = "Initial balance cannot be negative")
    private double balance;
}
