package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.dto.*;
import net.gowaka.gowaka.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Nnouka Stephen
 * @date 26 Sep 2019
 */
@RestController
@RequestMapping("/api/protected")
public class CarController {
    private CarService carService;

    @Autowired
    public CarController(CarService carService) {
        this.carService = carService;
    }

    @PreAuthorize("hasRole('ROLE_AGENCY_MANAGER') or hasRole('ROLE_AGENCY_ADMIN')")
    @PostMapping("/agency/car")
    public ResponseEntity<BusResponseDTO> addAgencyBus(@Valid @RequestBody BusDTO busDTO){
        return ResponseEntity.ok(carService.addOfficialAgencyBus(busDTO));
    }

    @PreAuthorize("hasRole('ROLE_USERS')")
    @PostMapping("/users/car")
    public ResponseEntity<SharedRideResponseDTO> addSharedRide(@Valid @RequestBody SharedRideDTO sharedRideDTO){
        return ResponseEntity.ok(carService.addSharedRide(sharedRideDTO));
    }

    @PreAuthorize("hasRole('ROLE_AGENCY_MANAGER') or hasRole('ROLE_AGENCY_ADMIN')")
    @GetMapping("/agency/car")
    public ResponseEntity<List<BusResponseDTO>> getAllOfficialAgencyBuses(){
        return ResponseEntity.ok(carService.getAllOfficialAgencyBuses());
    }

    @PreAuthorize("hasRole('ROLE_USERS')")
    @GetMapping("/users/car")
    public ResponseEntity<List<SharedRideResponseDTO>> getSharedRides() {
        return ResponseEntity.ok(carService.getAllSharedRides());
    }

    @PreAuthorize("hasRole('ROLE_GW_ADMIN')")
    @PostMapping("/car/{id}/approve")
    public ResponseEntity<Object> shallApprove(@Valid @RequestBody ApproveCarDTO approveCarDTO,
                                                      @PathVariable("id") String id){
        carService.approve(approveCarDTO, Long.parseLong(id));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ROLE_GW_ADMIN')")
    @GetMapping("/cars/unapproved")
    public ResponseEntity<List<CarDTO>> getAllUnapprovedCars(){
        return ResponseEntity.ok(carService.getAllUnapprovedCars());
    }
    @PreAuthorize("hasRole('ROLE_GW_ADMIN')")
    @GetMapping("/car/search")
    public ResponseEntity<CarDTO> searchByLicensePlateNumber(
            @RequestParam("licensePlateNumber") String licensePlateNumber){
        return ResponseEntity.ok(carService.searchByLicensePlateNumber(licensePlateNumber));
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_ADMIN', 'ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR')")
    @PostMapping("/agency/journeys/cars/{carId}")
    public ResponseEntity<JourneyResponseDTO> addJourney(
            @Valid @RequestBody JourneyDTO journeyDTO,
            @PathVariable("carId") String carId){
        return ResponseEntity.ok(carService.addJourney(journeyDTO, Long.parseLong(carId)));
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_ADMIN', 'ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR')")
    @PostMapping("/agency/journeys/{journeyId}/cars/{carId}")
    public ResponseEntity<JourneyResponseDTO> updateJourney(
            @Valid @RequestBody JourneyDTO journeyDTO,
            @PathVariable("journeyId") String journeyId,
            @PathVariable("carId") String carId){
        return ResponseEntity.ok(carService.updateJourney(journeyDTO, Long.parseLong(journeyId), Long.parseLong(carId)));
    }
}
