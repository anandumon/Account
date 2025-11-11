package com.bank.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePinRequest {
    @NotBlank(message = "Old PIN cannot be empty")
    @Size(min = 4, max = 4, message = "PIN must be 4 digits")
    private String oldPin;

    @NotBlank(message = "New PIN cannot be empty")
    @Size(min = 4, max = 4, message = "PIN must be 4 digits")
    private String newPin;
}
