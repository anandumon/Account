package com.bank.account.repository;

import com.bank.account.entity.MoneyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoneyRequestRepository extends JpaRepository<MoneyRequest, Long> {
}
