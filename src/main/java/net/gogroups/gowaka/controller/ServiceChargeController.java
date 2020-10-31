package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.dto.ServiceChargeDTO;
import net.gogroups.gowaka.service.ServiceChargeService;
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
public class ServiceChargeController {

    private ServiceChargeService serviceChargeService;

    public ServiceChargeController(ServiceChargeService serviceChargeService) {
        this.serviceChargeService = serviceChargeService;
    }

    @PreAuthorize("hasRole('ROLE_GW_ADMIN')")
    @GetMapping
    public ResponseEntity<List<ServiceChargeDTO>> getServiceCharge(){
        return ResponseEntity.ok(serviceChargeService.getServiceCharges());
    }

    @PreAuthorize("hasRole('ROLE_GW_ADMIN')")
    @PutMapping
    public ResponseEntity<?> updateServiceCharge(@RequestBody ServiceChargeDTO serviceChargeDTO){
        serviceChargeService.updateServiceCharge(serviceChargeDTO);
        return ResponseEntity.noContent().build();
    }

}
