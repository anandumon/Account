package com.bank.account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EmiConversionException extends RuntimeException {
    public EmiConversionException(String message) {
        super(message);
    }
}
