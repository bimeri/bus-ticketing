package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.dto.BusDTO;
import net.gowaka.gowaka.dto.ResponseBusDTO;
import net.gowaka.gowaka.dto.ResponseSharedRideDTO;
import net.gowaka.gowaka.dto.SharedRideDTO;
import net.gowaka.gowaka.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

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

    @PreAuthorize("hasRole('ROLE_AGENCY_MANAGER')")
    @PostMapping("/agency/car")
    public ResponseEntity<ResponseBusDTO> addAgencyBus(@Valid @RequestBody BusDTO busDTO){
        return ResponseEntity.ok(carService.addOfficialAgencyBus(busDTO));
    }

    @PreAuthorize("hasRole('ROLE_USERS')")
    @PostMapping("/user/car")
    public ResponseEntity<ResponseSharedRideDTO> addSharedRide(@Valid @RequestBody SharedRideDTO sharedRideDTO){
        return ResponseEntity.ok(carService.addSharedRide(sharedRideDTO));
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
