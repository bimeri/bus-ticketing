package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.dto.*;
import net.gogroups.gowaka.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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

    @PreAuthorize("hasRole('ROLE_AGENCY_MANAGER') or hasRole('ROLE_AGENCY_ADMIN') or hasRole('ROLE_AGENCY_OPERATOR')")
    @PostMapping("/agency/car")
    public ResponseEntity<BusResponseDTO> addAgencyBus(@RequestBody @Validated BusDTO busDTO) {
        return ResponseEntity.ok(carService.addOfficialAgencyBus(busDTO));
    }

    @PreAuthorize("hasRole('ROLE_USERS')")
    @PostMapping("/users/car")
    public ResponseEntity<SharedRideResponseDTO> addSharedRide(@RequestBody @Validated SharedRideDTO sharedRideDTO) {
        return ResponseEntity.ok(carService.addSharedRide(sharedRideDTO));
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_ADMIN', 'ROLE_AGENCY_OPERATOR')")
    @GetMapping("/agency/car")
    public ResponseEntity<List<BusResponseDTO>> getAllOfficialAgencyBuses() {
        return ResponseEntity.ok(carService.getAllOfficialAgencyBuses());
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_ADMIN', 'ROLE_AGENCY_OPERATOR')")
    @GetMapping("/agency/car/{carId}")
    public ResponseEntity<BusResponseDTO> getOfficialAgencyBuses(@PathVariable("carId") Long carId) {
        return ResponseEntity.ok(carService.getOfficialAgencyBuses(carId));
    }

    @PreAuthorize("hasRole('ROLE_USERS')")
    @GetMapping("/users/car")
    public ResponseEntity<List<SharedRideResponseDTO>> getSharedRides() {
        return ResponseEntity.ok(carService.getAllSharedRides());
    }

    @PreAuthorize("hasRole('ROLE_GW_ADMIN')")
    @PostMapping("/car/{id}/approve")
    public ResponseEntity<Object> shallApprove(@RequestBody @Validated ApproveCarDTO approveCarDTO,
                                               @PathVariable("id") String id) {
        carService.approve(approveCarDTO, Long.parseLong(id));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ROLE_GW_ADMIN')")
    @GetMapping("/cars/unapproved")
    public ResponseEntity<List<CarDTO>> getAllUnapprovedCars() {
        return ResponseEntity.ok(carService.getAllUnapprovedCars());
    }

    @PreAuthorize("hasRole('ROLE_GW_ADMIN')")
    @GetMapping("/car/search")
    public ResponseEntity<CarDTO> searchByLicensePlateNumber(
            @RequestParam("licensePlateNumber") String licensePlateNumber) {
        return ResponseEntity.ok(carService.searchByLicensePlateNumber(licensePlateNumber));
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_ADMIN', 'ROLE_AGENCY_OPERATOR')")
    @PostMapping("/agency/car/{carId}")
    public ResponseEntity updateAgencyCarInfo(
            @PathVariable("carId") Long carId, @RequestBody @Validated BusDTO busDTO) {
        carService.updateAgencyCarInfo(carId, busDTO);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_ADMIN', 'ROLE_AGENCY_OPERATOR')")
    @DeleteMapping("/agency/car/{carId}")
    public ResponseEntity deleteAgencyCarInfo(@PathVariable("carId") Long carId) {
        carService.deleteAgencyCarInfo(carId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/car/seat_structure")
    public ResponseEntity<List<SeatStructureDTO>> getSeatStructures(@RequestParam(value = "seatStructureCode", required = false) String seatStructureCode) {
        return ResponseEntity.ok(carService.getSeatStructures(seatStructureCode));
    }
}
