package com.bank.account.repository;

import com.bank.account.dto.PaymentStatus;
import com.bank.account.entity.CreditCardBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditCardBillRepository extends JpaRepository<CreditCardBill, Long> {
    Optional<CreditCardBill> findByCard_CardNumberAndBillingDate(String cardNumber, LocalDate billingDate);
    List<CreditCardBill> findByCard_CardNumberOrderByBillingDateDesc(String cardNumber);
    Optional<CreditCardBill> findTopByCard_CardNumberAndPaymentStatusNotOrderByBillingDateDesc(String cardNumber, PaymentStatus paymentStatus);
}
