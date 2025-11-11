package com.bank.account.controller;

import com.bank.account.dto.EMIConversionRequest;
import com.bank.account.dto.EMIOfferResponse;
import com.bank.account.entity.EMIPlan;
import com.bank.account.entity.EMISchedule;
import com.bank.account.service.EMIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emi")
public class EMIController {

    @Autowired
    private EMIService emiService;

    @GetMapping("/offers/{transactionId}")
    public EMIOfferResponse getEmiOffers(@PathVariable Long transactionId) {
        return emiService.getEmiOffers(transactionId);
    }

    @PostMapping("/convert")
    public EMIPlan convertToEmi(@RequestBody EMIConversionRequest request) {
        return emiService.convertToEmi(request);
    }

    @GetMapping("/plan/{emiPlanId}")
    public EMIPlan getEmiPlanDetails(@PathVariable Long emiPlanId) {
        return emiService.getEmiPlanDetails(emiPlanId);
    }

    @GetMapping("/plan/{emiPlanId}/schedule")
    public List<EMISchedule> getEmiSchedule(@PathVariable Long emiPlanId) {
        return emiService.getEmiSchedule(emiPlanId);
    }
}
