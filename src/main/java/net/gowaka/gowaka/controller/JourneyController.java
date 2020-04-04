package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.dto.*;
import net.gowaka.gowaka.service.JourneyService;
import org.hibernate.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
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
            @RequestBody @Valid JourneyDepartureIndicatorDTO journeyDepartureIndicatorDTO,
            @PathVariable("journeyId") Long journeyId){
        journeyService.updateJourneyDepartureIndicator(journeyId, journeyDepartureIndicatorDTO);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR')")
    @PostMapping("/agency/journeys/{journeyId}/arrival")
    public ResponseEntity updateJourneyArrivalIndicator(
            @RequestBody @Valid JourneyArrivalIndicatorDTO journeyArrivalIndicatorDTO,
            @PathVariable("journeyId") Long journeyId){
        journeyService.updateJourneyArrivalIndicator(journeyId, journeyArrivalIndicatorDTO);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR')")
    @DeleteMapping("/agency/journeys/{journeyId}/transitAndStops/{transitAndStopId}")
    public ResponseEntity removeNonBookedStop(
            @PathVariable("journeyId") Long journeyId, @PathVariable("transitAndStopId") Long stopId) {
        journeyService.removeNonBookedStop(journeyId, stopId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ROLE_USERS')")
    @PostMapping("/users/journeys/cars/{carId}")
    public ResponseEntity<JourneyResponseDTO> addSharedRideJourney(@PathVariable("carId") Long carId, @Valid @RequestBody JourneyDTO journeyDTO){
        return ResponseEntity.ok(journeyService.addSharedJourney(journeyDTO, carId));
    }

    @PreAuthorize("hasRole('ROLE_USERS')")
    @PostMapping("/users/journeys/{journeyId}/cars/{carId}")
    public ResponseEntity<JourneyResponseDTO> updateSharedRideJourney(
            @PathVariable("journeyId") Long journeyId,
            @PathVariable("carId") Long carId,
            @Valid @RequestBody JourneyDTO journeyDTO){
        return ResponseEntity.ok(journeyService.updateSharedJourney(journeyDTO, journeyId, carId));
    }
    @PreAuthorize("hasRole('ROLE_USERS')")
    @GetMapping("/users/journeys/{journeyId}")
    public ResponseEntity<JourneyResponseDTO> getSharedRideById(@PathVariable("journeyId") Long journeyId){
        return ResponseEntity.ok(journeyService.getSharedJourneyById(journeyId));
    }
    @PreAuthorize("hasRole('ROLE_USERS')")
    @GetMapping("/users/journeys")
    public ResponseEntity<List<JourneyResponseDTO>> getAllPersonalAgencyJourneys(){
        return ResponseEntity.ok(journeyService.getAllPersonalAgencyJourneys());
    }
    @PreAuthorize("hasRole('ROLE_USERS')")
    @PostMapping("/users/journeys/{journeyId}/departure")
    public ResponseEntity updateSharedJourneyDepartureIndicator(
            @PathVariable("journeyId") Long journeyId,
            @RequestBody @Valid JourneyDepartureIndicatorDTO departureIndicatorDTO){
        journeyService.updateSharedJourneyDepartureIndicator(journeyId, departureIndicatorDTO);
        return ResponseEntity.noContent().build();
    }
    @PreAuthorize("hasRole('ROLE_USERS')")
    @PostMapping("/users/journeys/{journeyId}/arrival")
    public ResponseEntity updateShareJourneyArrivalIndicator(
            @PathVariable("journeyId") Long journeyId,
            @RequestBody @Valid JourneyArrivalIndicatorDTO arrivalIndicatorDTO){
        journeyService.updateSharedJourneyArrivalIndicator(journeyId, arrivalIndicatorDTO);
        return ResponseEntity.noContent().build();
    }
    @PreAuthorize("hasAnyRole('ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR')")
    @DeleteMapping("/users/journeys/{journeyId}")
    public ResponseEntity deleteNonBookedSharedJourney(
            @PathVariable("journeyId") Long journeyId){
        journeyService.deleteNonBookedSharedJourney(journeyId);
        return ResponseEntity.noContent().build();
    }
    @PreAuthorize("hasRole('ROLE_USERS')")
    @PostMapping("/users/journeys/{journeyId}/add_stops")
    public ResponseEntity addStopsInPersonalAgency(@PathVariable("journeyId") Long journeyId,
                                                   @Valid @RequestBody AddStopDTO addStopDTO ){
        journeyService.addStopToPersonalAgency(journeyId, addStopDTO);
        return ResponseEntity.noContent().build();

    }


}
