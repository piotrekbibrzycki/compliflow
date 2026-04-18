package com.compliflow.account_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 34)
    private String accountNumber;

    @Column(nullable = false, length = 100)
    private String ownerName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    @Builder.Default
    private Integer riskScore = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean anonymized = false;

    private LocalDateTime anonymizedAt;

    @Column(length = 128)
    private String walletAddress;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.riskScore == null) {
            this.riskScore = 0;
        }
        if (this.anonymized == null) {
            this.anonymized = false;
        }
    }
}