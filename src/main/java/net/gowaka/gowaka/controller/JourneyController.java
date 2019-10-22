package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.dto.*;
import net.gowaka.gowaka.service.JourneyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Nnouka Stephen
 * @date 17 Oct 2019
 */
@RestController
@RequestMapping("/api/protected")
public class JourneyController {
    private JourneyService journeyService;

    @Autowired
    public JourneyController(JourneyService journeyService) {
        this.journeyService = journeyService;
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_ADMIN', 'ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR')")
    @PostMapping("/agency/journeys/cars/{carId}")
    public ResponseEntity<JourneyResponseDTO> addJourney(
            @Valid @RequestBody JourneyDTO journeyDTO,
            @PathVariable("carId") String carId){
        return ResponseEntity.ok(journeyService.addJourney(journeyDTO, Long.parseLong(carId)));
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_ADMIN', 'ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR')")
    @PostMapping("/agency/journeys/{journeyId}/cars/{carId}")
    public ResponseEntity<JourneyResponseDTO> updateJourney(
            @Valid @RequestBody JourneyDTO journeyDTO,
            @PathVariable("journeyId") String journeyId,
            @PathVariable("carId") String carId){
        return ResponseEntity.ok(journeyService.updateJourney(journeyDTO, Long.parseLong(journeyId), Long.parseLong(carId)));
    }
    @PreAuthorize("hasAnyRole('ROLE_AGENCY_ADMIN', 'ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR', 'AGENCY_BOOKING')")
    @GetMapping("/agency/journeys")
    public ResponseEntity<List<JourneyResponseDTO>> getAllOfficialAgencyJourneys(){
        return ResponseEntity.ok(journeyService.getAllOfficialAgencyJourneys());
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_ADMIN', 'ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR', 'AGENCY_BOOKING')")
    @GetMapping("/agency/journeys/{journeyId}")
    public ResponseEntity<JourneyResponseDTO> getJourneyById(@PathVariable("journeyId") Long journeyId){
        return ResponseEntity.ok(journeyService.getJourneyById(journeyId));
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_ADMIN', 'ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR')")
    @PostMapping("/agency/journeys/{journeyId}/add_stops")
    public ResponseEntity addStops(@PathVariable("journeyId") Long journeyId,
                                   @Valid @RequestBody AddStopDTO addStopDTO ){
        journeyService.addStop(journeyId, addStopDTO);
        return ResponseEntity.noContent().build();
    }
    @PreAuthorize("hasAnyRole('ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR')")
    @DeleteMapping("/agency/journeys/{journeyId}")
    public ResponseEntity deleteJourney(@PathVariable("journeyId") Long journeyId){
        journeyService.deleteNonBookedJourney(journeyId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR')")
    @PostMapping("/agency/journeys/{journeyId}/departure")
    public ResponseEntity updateJourneyDepartureIndicator(
            @RequestBody JourneyDepartureIndicatorDTO journeyDepartureIndicatorDTO,
            @PathVariable("journeyId") Long journeyId){
        journeyService.updateJourneyDepartureIndicator(journeyId, journeyDepartureIndicatorDTO);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR')")
    @PostMapping("/agency/journeys/{journeyId}/arrival")
    public ResponseEntity updateJourneyArrivalIndicator(
            @RequestBody JourneyArrivalIndicatorDTO journeyArrivalIndicatorDTO,
            @PathVariable("journeyId") Long journeyId){
        journeyService.updateJourneyArrivalIndicator(journeyId, journeyArrivalIndicatorDTO);
        return ResponseEntity.noContent().build();
    }
}
