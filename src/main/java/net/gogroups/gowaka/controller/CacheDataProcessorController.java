package net.gogroups.gowaka.controller;

import lombok.RequiredArgsConstructor;
import net.gogroups.gowaka.dto.AllAvailableJourneyAndBookedSeatsDTO;
import net.gogroups.gowaka.service.CacheDataProcessorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 8:19 PM <br/>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CacheDataProcessorController {

    private final CacheDataProcessorService cacheDataProcessorService;

    @PreAuthorize("hasRole('ROLE_GW_ADMIN')")
    @GetMapping("/protected/cache/journey")
    public ResponseEntity<AllAvailableJourneyAndBookedSeatsDTO> getAllAvailableJourneys() {
        return ResponseEntity.ok(cacheDataProcessorService.getAllAvailableJourneys());
    }

}
