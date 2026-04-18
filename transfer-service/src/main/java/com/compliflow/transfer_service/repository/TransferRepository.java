package com.compliflow.transfer_service.repository;

import com.compliflow.transfer_service.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findByFromAccountOrToAccount(String fromAccount, String toAccount);

    long countByFromAccountAndCreatedAtAfter(String fromAccount, LocalDateTime createdAt);

}