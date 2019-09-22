package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.dto.OfficialAgencyUserDTO;
import net.gowaka.gowaka.dto.OfficialAgencyUserRoleRequestDTO;
import net.gowaka.gowaka.dto.CreateOfficialAgencyDTO;
import net.gowaka.gowaka.dto.OfficialAgencyDTO;
import net.gowaka.gowaka.service.OfficialAgencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 8:19 PM <br/>
 */
@RestController
@RequestMapping("/api")
public class OfficialAgencyController {

    private OfficialAgencyService officialAgencyService;

    @Autowired
    public OfficialAgencyController(OfficialAgencyService officialAgencyService) {
        this.officialAgencyService = officialAgencyService;
    }

    @PreAuthorize("hasRole('ROLE_GW_ADMIN')")
    @PostMapping("/protected/agency")
    ResponseEntity<OfficialAgencyDTO> createOfficialAgency(@RequestBody CreateOfficialAgencyDTO createOfficialAgencyDTO){
        return ResponseEntity.ok(officialAgencyService.createOfficialAgency(createOfficialAgencyDTO));
    }

    @PreAuthorize("hasRole('ROLE_AGENCY_ADMIN')")
    @PostMapping("/protected/agency/user/role")
    ResponseEntity<OfficialAgencyUserDTO> assignAgencyUserRole(@RequestBody OfficialAgencyUserRoleRequestDTO officialAgencyUserRoleRequestDTO){
        return ResponseEntity.ok(officialAgencyService.assignAgencyUserRole(officialAgencyUserRoleRequestDTO));
    }

}
