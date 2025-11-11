package com.bank.account.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateCardLimitRequest {
    @Min(value = 0, message = "New limit cannot be negative")
    private double newLimit;
}
