package com.bank.account.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class EMISchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "emi_plan_id", nullable = false)
    private EMIPlan emiPlan;

    @Column(nullable = false)
    private int installmentNumber;

    @Column(nullable = false)
    private double principalAmount;

    @Column(nullable = false)
    private double interestAmount;

    @Column(nullable = false)
    private double totalInstallmentAmount;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private String status; // PENDING, PAID, OVERDUE
}
