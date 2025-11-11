package com.bank.account.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayBillRequest {
    @NotNull(message = "Payment option cannot be null")
    private PaymentOption paymentOption;

    @Min(value = 0, message = "Amount cannot be negative")
    private double amount; // Only used if paymentOption is OTHER_AMOUNT
}
