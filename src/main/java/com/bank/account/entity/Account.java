package com.bank.account.entity;

import com.bank.account.dto.AccountStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Account number cannot be empty")
    @Pattern(regexp = "^[0-9]{10,18}$", message = "Account number must be 10 to 18 digits")
    @Column(unique = true, nullable = false)
    private String accountNumber;

    @NotBlank(message = "Account type cannot be empty")
    @Column(nullable = false)
    private String accountType; // e.g., SAVINGS, CURRENT

    @NotBlank(message = "IFSC Code cannot be empty")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC Code format")
    @Column(nullable = false)
    private String ifscCode;

    @NotBlank(message = "Branch cannot be empty")
    @Column(nullable = false)
    private String branch;

    @Min(value = 0, message = "Balance cannot be negative")
    private double balance;

    @NotNull(message = "Account status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    private Customer customer;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cards;
}
