package net.gogroups.gowaka.controller;

import lombok.extern.slf4j.Slf4j;
import net.gogroups.gowaka.dto.ServiceChargeDTO;
import net.gogroups.gowaka.service.ServiceChargeService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/31/20 5:57 AM <br/>
 */
@RestController
@RequestMapping("/api/protected/service_charges")
@Slf4j
public class ServiceChargeController {

    private ServiceChargeService serviceChargeService;

    public ServiceChargeController(ServiceChargeService serviceChargeService) {
        this.serviceChargeService = serviceChargeService;
    }

    @PreAuthorize("hasRole('USERS')")
    @GetMapping
    public ResponseEntity<List<ServiceChargeDTO>> getServiceCharge() {
        return ResponseEntity.ok(serviceChargeService.getServiceCharges());
    }

    @PreAuthorize("hasRole('ROLE_GW_ADMIN')")
    @PutMapping
    public ResponseEntity<?> updateServiceCharge(@RequestBody ServiceChargeDTO serviceChargeDTO) {
        serviceChargeService.updateServiceCharge(serviceChargeDTO);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("api/public/service_charges")
    @CacheEvict(value = "service_charges", allEntries = true)
    public ResponseEntity<String> evictServiceChargeCache() {
        log.info("evicting service_charges from cache");
        return ResponseEntity.ok("Cache evicted");
    }

}
