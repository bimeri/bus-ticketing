package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.dto.*;
import net.gogroups.gowaka.service.OfficialAgencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    ResponseEntity<OfficialAgencyDTO> createOfficialAgency(@RequestBody CreateOfficialAgencyDTO createOfficialAgencyDTO) {
        return ResponseEntity.ok(officialAgencyService.createOfficialAgency(createOfficialAgencyDTO));
    }

    @PreAuthorize("hasRole('ROLE_GW_ADMIN')")
    @PutMapping("/protected/agency/{id}")
    public ResponseEntity<Void> updateOfficialAgency(@PathVariable("id") Long agencyId, @RequestBody OfficialAgencyDTO officialAgencyDTO) {
        officialAgencyService.updateOfficialAgency(agencyId, officialAgencyDTO);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ROLE_GW_ADMIN')")
    @GetMapping("/protected/agency")
    public ResponseEntity<List<OfficialAgencyDTO>> getOfficialAgencies() {
        return ResponseEntity.ok(officialAgencyService.getAllAgencies());
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_ADMIN','ROLE_AGENCY_MANAGER','ROLE_AGENCY_OPERATOR','ROLE_AGENCY_BOOKING', 'ROLE_AGENCY_CHECKING')")
    @GetMapping("/protected/agency/user_agency")
    public ResponseEntity<OfficialAgencyDTO> getUserOfficialAgency() {
        return ResponseEntity.ok(officialAgencyService.getUserAgency());
    }

    @PreAuthorize("hasRole('ROLE_GW_ADMIN')")
    @PostMapping("/protected/agency/{id}/logo")
    public ResponseEntity<OfficialAgencyDTO> uploadAgencyLogo(@PathVariable("id") Long agencyId, @RequestParam("logo") MultipartFile file) {
        officialAgencyService.uploadAgencyLogo(agencyId, file);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ROLE_AGENCY_ADMIN')")
    @PostMapping("/protected/agency/user/role")
    ResponseEntity<OfficialAgencyUserDTO> assignAgencyUserRole(@RequestBody OfficialAgencyUserRoleRequestDTO officialAgencyUserRoleRequestDTO) {
        return ResponseEntity.ok(officialAgencyService.assignAgencyUserRole(officialAgencyUserRoleRequestDTO));
    }

    @PreAuthorize("hasRole('ROLE_AGENCY_ADMIN')")
    @GetMapping("/protected/agency/user")
    ResponseEntity<List<OfficialAgencyUserDTO>> getAgencyUsers() {
        return ResponseEntity.ok(officialAgencyService.getAgencyUsers());
    }

    @PreAuthorize("hasRole('ROLE_AGENCY_ADMIN')")
    @PostMapping("/protected/agency/branch/{branchId}/user")
    ResponseEntity<OfficialAgencyUserDTO> addAgencyUser(@RequestBody EmailDTO emailDTO, @PathVariable("branchId") Long branchId) {
        return ResponseEntity.ok(officialAgencyService.addAgencyUser(emailDTO, branchId));
    }

    @PreAuthorize("hasRole('ROLE_AGENCY_ADMIN')")
    @DeleteMapping("/protected/agency/user/{userId}")
    ResponseEntity<?> removeAgencyUser(@PathVariable("userId") String userId) {
        officialAgencyService.removeAgencyUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ROLE_AGENCY_ADMIN')")
    @PostMapping("/protected/agency/branch")
    ResponseEntity<Void> createBranch(@RequestBody @Validated CreateBranchDTO createBranchDTO) {
        officialAgencyService.createBranch(createBranchDTO);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ROLE_AGENCY_ADMIN')")
    @PutMapping("/protected/agency/branch/{branchId}")
    ResponseEntity<Void> updateBranch(@RequestBody @Validated CreateBranchDTO createBranchDTO, @PathVariable("branchId") Long branchId) {
        officialAgencyService.updateBranch(createBranchDTO, branchId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ROLE_AGENCY_ADMIN')")
    @DeleteMapping("/protected/agency/branch/{branchId}")
    ResponseEntity<Void> deleteBranch(@PathVariable("branchId") Long branchId) {
        officialAgencyService.deleteBranch(branchId);
        return ResponseEntity.noContent().build();
    }

}
