package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.domain.model.Bus;
import net.gowaka.gowaka.dto.ApproveCarDTO;
import net.gowaka.gowaka.dto.BusDTO;
import net.gowaka.gowaka.dto.ResponseBusDTO;
import net.gowaka.gowaka.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/agency/car")
    public ResponseEntity<ResponseBusDTO> addAgencyBus(@Valid @RequestBody BusDTO busDTO){
        return ResponseEntity.ok(carService.addOfficialAgencyBus(busDTO));
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
