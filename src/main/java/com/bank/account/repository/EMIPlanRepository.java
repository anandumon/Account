package com.bank.account.repository;

import com.bank.account.entity.EMIPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EMIPlanRepository extends JpaRepository<EMIPlan, Long> {
    Optional<EMIPlan> findByOriginalTransaction_Id(Long transactionId);
    List<EMIPlan> findByCard_CardNumber(String cardNumber);
}
