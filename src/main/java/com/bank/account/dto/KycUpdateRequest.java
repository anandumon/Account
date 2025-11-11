package com.bank.account.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KycUpdateRequest {
    @NotNull(message = "KYC completion status cannot be null")
    private boolean kycCompleted;
}
