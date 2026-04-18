package com.compliflow.transfer_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "restricted_party_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestrictedPartyEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestrictedMatchType matchType;

    @Column(nullable = false, length = 128)
    private String matchValue;

    @Column(nullable = false, length = 255)
    private String entityName;

    @Column(nullable = false, length = 100)
    private String source;

    @Column(length = 255)
    private String sourceReference;

    private LocalDateTime lastSyncedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.active == null) {
            this.active = true;
        }
    }
}