package com.bank.account.service;

import com.bank.account.dto.EMIConversionRequest;
import com.bank.account.dto.EMIOfferResponse;
import com.bank.account.entity.EMIPlan;
import com.bank.account.entity.EMISchedule;

import java.time.LocalDate;
import java.util.List;

public interface EMIService {

    EMIOfferResponse getEmiOffers(Long transactionId);
    EMIPlan convertToEmi(EMIConversionRequest request);
    EMIPlan getEmiPlanDetails(Long emiPlanId);
    List<EMISchedule> getEmiSchedule(Long emiPlanId);
    void processMonthlyEmiInstallments(LocalDate date); // Scheduled task
}
