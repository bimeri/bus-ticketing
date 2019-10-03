package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.dto.BusDTO;
import net.gowaka.gowaka.dto.ResponseBusDTO;
import net.gowaka.gowaka.dto.ResponseSharedRideDTO;
import net.gowaka.gowaka.dto.SharedRideDTO;
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
    public ResponseEntity<ResponseBusDTO> addAgencyBus(@Valid @RequestBody BusDTO busDTO){
        return ResponseEntity.ok(carService.addOfficialAgencyBus(busDTO));
    }

    @PreAuthorize("hasRole('ROLE_USERS')")
    @PostMapping("/users/car")
    public ResponseEntity<ResponseSharedRideDTO> addSharedRide(@Valid @RequestBody SharedRideDTO sharedRideDTO){
        return ResponseEntity.ok(carService.addSharedRide(sharedRideDTO));
    }

    @PreAuthorize("hasRole('ROLE_AGENCY_MANAGER') or hasRole('ROLE_AGENCY_ADMIN')")
    @GetMapping("/agency/car")
    public ResponseEntity<List<ResponseBusDTO>> getAllOfficialAgencyBuses(){
        return ResponseEntity.ok(carService.getAllOfficialAgencyBuses());
    }

    @PreAuthorize("hasRole('ROLE_USERS')")
    @GetMapping("/users/car")
    public ResponseEntity<List<ResponseSharedRideDTO>> getSharedRides() {
        return ResponseEntity.ok(carService.getAllSharedRides());
    }
/*
    @PostMapping("/{id}/approve")
    public ResponseEntity<Object> shallApprove(@RequestBody ApproveCarDTO approveCarDTO,
                                                      @PathVariable("id") String id){
        carService.approve(approveCarDTO, Long.parseLong(id));
        return ResponseEntity.noContent().build();
    }

 */
}
