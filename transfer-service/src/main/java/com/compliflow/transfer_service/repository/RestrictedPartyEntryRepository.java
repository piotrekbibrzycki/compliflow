package com.compliflow.transfer_service.repository;

import com.compliflow.transfer_service.model.RestrictedMatchType;
import com.compliflow.transfer_service.model.RestrictedPartyEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestrictedPartyEntryRepository extends JpaRepository<RestrictedPartyEntry, Long> {

    Optional<RestrictedPartyEntry> findFirstByMatchTypeAndMatchValueAndActiveTrue(RestrictedMatchType matchType, String matchValue);

    List<RestrictedPartyEntry> findAllByOrderByCreatedAtDesc();

    List<RestrictedPartyEntry> findAllByActiveTrueOrderByCreatedAtDesc();
}