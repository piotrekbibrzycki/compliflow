package com.compliflow.transfer_service.controller;

import com.compliflow.transfer_service.dto.RestrictedPartyEntryRequestDto;
import com.compliflow.transfer_service.dto.RestrictedPartyEntryResponseDto;
import com.compliflow.transfer_service.service.RestrictedPartyEntryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restricted-parties")
public class RestrictedPartyEntryController {

    private final RestrictedPartyEntryService restrictedPartyEntryService;

    public RestrictedPartyEntryController(RestrictedPartyEntryService restrictedPartyEntryService) {
        this.restrictedPartyEntryService = restrictedPartyEntryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestrictedPartyEntryResponseDto create(@Valid @RequestBody RestrictedPartyEntryRequestDto request) {
        return restrictedPartyEntryService.create(request);
    }

    @GetMapping
    public List<RestrictedPartyEntryResponseDto> getAll(
            @RequestParam(name = "activeOnly", defaultValue = "false") boolean activeOnly
    ) {
        return activeOnly
                ? restrictedPartyEntryService.getActive()
                : restrictedPartyEntryService.getAll();
    }

    @PatchMapping("/{id}/deactivate")
    public RestrictedPartyEntryResponseDto deactivate(@PathVariable Long id) {
        return restrictedPartyEntryService.deactivate(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        restrictedPartyEntryService.delete(id);
    }
}