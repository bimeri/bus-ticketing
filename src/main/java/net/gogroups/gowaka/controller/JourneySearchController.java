package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.dto.JourneyResponseDTO;
import net.gogroups.gowaka.service.JourneyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 6/20/20 11:23 AM <br/>
 */
@RestController
@RequestMapping("/api")
public class JourneySearchController {

    private JourneyService journeyService;

    @Autowired
    public JourneySearchController(JourneyService journeyService) {
        this.journeyService = journeyService;
    }

    @GetMapping("/public/journey/search/departure/{departureLocationID}/destination/{destinationLocationID}")
    public ResponseEntity<List<JourneyResponseDTO>> searchJourney(@PathVariable("departureLocationID") Long departureLocationId,
                                                                  @PathVariable("destinationLocationID") Long destinationLocationId,
                                                                  @RequestParam("time") String time) {
        return ResponseEntity.ok(journeyService.searchJourney(departureLocationId, destinationLocationId, time));
    }

    @PreAuthorize("hasRole('ROLE_USERS')")
    @GetMapping("/protected/journey/search")
    public ResponseEntity<List<JourneyResponseDTO>> searchJourney() {
        return ResponseEntity.ok(journeyService.searchJourney());
    }

    @GetMapping("/public/journey/search")
    public ResponseEntity<List<JourneyResponseDTO>> getAllAvailableJourney() {
        return ResponseEntity.ok(journeyService.searchAllAvailableJourney());
    }

}
