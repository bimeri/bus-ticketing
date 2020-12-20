package net.gogroups.gowaka.controller;

import lombok.extern.slf4j.Slf4j;
import net.gogroups.gowaka.dto.RefundDTO;
import net.gogroups.gowaka.dto.RequestRefundDTO;
import net.gogroups.gowaka.dto.ResponseRefundDTO;
import net.gogroups.gowaka.service.RefundService;
import net.gogroups.security.service.AuthorizedUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 12/17/20 8:12 AM <br/>
 */
@RestController
@RequestMapping("/api/protected")
@Slf4j
public class RefundController {

    private RefundService refundService;
    private AuthorizedUserService authorizedUserService;

    @Autowired
    public RefundController(RefundService refundService, AuthorizedUserService authorizedUserService) {
        this.refundService = refundService;
        this.authorizedUserService = authorizedUserService;
    }

    @PreAuthorize("hasAnyRole('ROLE_USERS')")
    @PostMapping("/bookedJourneys/refund")
    public ResponseEntity<Void> requestRefund(@RequestBody @Validated RequestRefundDTO requestRefundDTO) {
        String userId = authorizedUserService.getUserDetails().getId();
        log.info("requesting refund userId: {}", userId);
        refundService.requestRefund(requestRefundDTO, userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_USERS')")
    @GetMapping("/bookedJourneys/refund/{id}/user")
    public ResponseEntity<RefundDTO> getUserRefundRequest(@PathVariable("id") Long refundId) {
        String userId = authorizedUserService.getUserDetails().getId();
        log.info("getting userId refunds, refundId: {}", refundId);
        return ResponseEntity.ok(refundService.getUserRefund(refundId, userId));
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_ADMIN','ROLE_AGENCY_MANAGER')")
    @PostMapping("/bookedJourneys/refund/{id}")
    public ResponseEntity<Void> responseRefund(@PathVariable("id") Long refundId, @RequestBody @Validated ResponseRefundDTO responseRefundDTO) {
        String userId = authorizedUserService.getUserDetails().getId();
        log.info("responding to refund userId: {}, refundId: {}", userId, refundId);
        refundService.responseRefund(refundId, responseRefundDTO, userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_ADMIN','ROLE_AGENCY_MANAGER','ROLE_AGENCY_OPERATOR', 'ROLE_AGENCY_BOOKING')")
    @GetMapping("/bookedJourneys/journey/{id}/refund")
    public ResponseEntity<List<RefundDTO>> getAllJourneyRefunds(@PathVariable("id") Long journeyId) {
        String userId = authorizedUserService.getUserDetails().getId();
        log.info("get all agency journey request user: {}, journey: {}", userId, journeyId);
        return ResponseEntity.ok(refundService.getAllJourneyRefunds(journeyId, userId));
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_ADMIN','ROLE_AGENCY_MANAGER','ROLE_AGENCY_OPERATOR', 'ROLE_AGENCY_BOOKING')")
    @PostMapping("/bookedJourneys/refund/{id}/refunded")
    public ResponseEntity<Void> refunded(@PathVariable("id") Long refundId) {
        String userId = authorizedUserService.getUserDetails().getId();
        log.info("refund refundId: {}, by userId: {},", refundId, userId);
        refundService.refunded(refundId, userId);
        return ResponseEntity.noContent().build();
    }

}
