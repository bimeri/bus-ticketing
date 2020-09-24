package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.network.api.cbs.model.CBSBenefitDTO;
import net.gogroups.gowaka.network.api.cbs.model.CBSRewardPointDTO;
import net.gogroups.gowaka.service.CBSService;
import net.gogroups.gowaka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 6/21/20 5:48 PM <br/>
 */
@RestController
@RequestMapping("/api")
public class CBSController {

    private CBSService cbsService;
    private UserService userService;

    @Autowired
    public CBSController(CBSService cbsService, UserService userService) {
        this.cbsService = cbsService;
        this.userService = userService;
    }

    @GetMapping("/public/cbs/benefits")
    public ResponseEntity<List<CBSBenefitDTO>> getAllBenefits() {
        return ResponseEntity.ok(cbsService.getAllAvailableBenefit());
    }

    @PreAuthorize("hasAnyRole('ROLE_USERS')")
    @GetMapping("/protected/cbs/benefits/user")
    public ResponseEntity<List<CBSBenefitDTO>> getAllUserBenefits() {
        String userId = userService.getCurrentAuthUser().getId();
        return ResponseEntity.ok(cbsService.getAllUserAvailableBenefit(userId));
    }

    @PreAuthorize("hasAnyRole('ROLE_USERS')")
    @GetMapping("/protected/cbs/reward_points/user")
    public ResponseEntity<CBSRewardPointDTO> getUserRewardPoints() {
        String userId = userService.getCurrentAuthUser().getId();
        return ResponseEntity.ok(cbsService.getUserRewardPoints(userId));
    }


}
