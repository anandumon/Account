package com.bank.account.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IssueCardRequest {
    @NotNull(message = "Customer ID cannot be null")
    private Long customerId;

    @NotNull(message = "Account ID cannot be null")
    private Long accountId;

    @NotNull(message = "Card type cannot be null")
    private CardType cardType;

    @Min(value = 0, message = "Limit cannot be negative")
    private double limit; // Credit limit for CREDIT, daily withdrawal limit for DEBIT
}
