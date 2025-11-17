package com.bank.account.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
public class EMIPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "original_transaction_id", nullable = false)
    private Transaction originalTransaction;

    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(nullable = false)
    private double originalAmount;

    @Column(nullable = false)
    private int tenureMonths;

    @Column(nullable = false)
    private double interestRate; // e.g., 0.16 for 16%

    @Column(nullable = false)
    private double processingFeePercentage; // e.g., 0.025 for 2.5%

    @Column(nullable = false)
    private double totalPayableAmount;

    @Column(nullable = false)
    private double monthlyInstallmentAmount;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private String status; // ACTIVE, COMPLETED, CANCELLED

    @OneToMany(mappedBy = "emiPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EMISchedule> emiSchedule;
}
