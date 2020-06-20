package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.dto.JourneyResponseDTO;
import net.gowaka.gowaka.service.JourneyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 6/20/20 11:23 AM <br/>
 */
@RestController
@RequestMapping("/api/public/journey/search")
public class JourneySearchController {

    private JourneyService journeyService;

    @Autowired
    public JourneySearchController(JourneyService journeyService) {
        this.journeyService = journeyService;
    }

    @GetMapping("/departure/{departureLocationID}/destination/{destinationLocationID}")
    public ResponseEntity<List<JourneyResponseDTO>> searchJourney(@PathVariable("departureLocationID") Long departureLocationId,
                                                                  @PathVariable("destinationLocationID") Long destinationLocationId,
                                                                  @RequestParam("time") String time) {
        return ResponseEntity.ok(journeyService.searchJourney(departureLocationId, destinationLocationId, time));
    }

    @GetMapping()
    public ResponseEntity<List<JourneyResponseDTO>> searchJourney() {
        return ResponseEntity.ok(journeyService.searchJourney());
    }
}
