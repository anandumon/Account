package com.bank.account.repository;

import com.bank.account.entity.EMISchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EMIScheduleRepository extends JpaRepository<EMISchedule, Long> {
    List<EMISchedule> findByEmiPlan_IdOrderByInstallmentNumberAsc(Long emiPlanId);
    List<EMISchedule> findByEmiPlan_Card_CardNumberAndStatus(String cardNumber, String status);
}
