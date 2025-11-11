package com.bank.account.entity;

import com.bank.account.dto.RequestStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class MoneyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Account requester;

    @ManyToOne
    private Account payer;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}
