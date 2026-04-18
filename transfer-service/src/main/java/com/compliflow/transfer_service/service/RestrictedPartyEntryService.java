package com.compliflow.transfer_service.service;

import com.compliflow.transfer_service.dto.RestrictedPartyEntryRequestDto;
import com.compliflow.transfer_service.dto.RestrictedPartyEntryResponseDto;
import com.compliflow.transfer_service.model.RestrictedPartyEntry;
import com.compliflow.transfer_service.repository.RestrictedPartyEntryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RestrictedPartyEntryService {

    private final RestrictedPartyEntryRepository restrictedPartyEntryRepository;

    public RestrictedPartyEntryService(RestrictedPartyEntryRepository restrictedPartyEntryRepository) {
        this.restrictedPartyEntryRepository = restrictedPartyEntryRepository;
    }

    @Transactional
    public RestrictedPartyEntryResponseDto create(RestrictedPartyEntryRequestDto request) {
        RestrictedPartyEntry entry = RestrictedPartyEntry.builder()
                .matchType(request.getMatchType())
                .matchValue(request.getMatchValue())
                .entityName(request.getEntityName())
                .source(request.getSource())
                .sourceReference(request.getSourceReference())
                .lastSyncedAt(LocalDateTime.now())
                .active(true)
                .build();

        return RestrictedPartyEntryResponseDto.from(restrictedPartyEntryRepository.save(entry));
    }

    public List<RestrictedPartyEntryResponseDto> getAll() {
        return restrictedPartyEntryRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(RestrictedPartyEntryResponseDto::from)
                .toList();
    }

    public List<RestrictedPartyEntryResponseDto> getActive() {
        return restrictedPartyEntryRepository.findAllByActiveTrueOrderByCreatedAtDesc().stream()
                .map(RestrictedPartyEntryResponseDto::from)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        if (!restrictedPartyEntryRepository.existsById(id)) {
            throw new EntityNotFoundException("Restricted party entry not found: " + id);
        }
        restrictedPartyEntryRepository.deleteById(id);
    }

    @Transactional
    public RestrictedPartyEntryResponseDto deactivate(Long id) {
        RestrictedPartyEntry entry = restrictedPartyEntryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restricted party entry not found: " + id));

        entry.setActive(false);
        entry.setLastSyncedAt(LocalDateTime.now());

        return RestrictedPartyEntryResponseDto.from(restrictedPartyEntryRepository.save(entry));
    }
}