package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.dto.LocationResponseDTO;
import net.gogroups.gowaka.service.TransitAndStopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Nnouka Stephen
 * @date 09 Oct 2019
 */
@RestController
@RequestMapping("api/public")
public class TransitAndStopPublicController {
    private TransitAndStopService transitAndStopService;

    @Autowired
    public TransitAndStopPublicController(TransitAndStopService transitAndStopService) {
        this.transitAndStopService = transitAndStopService;
    }

    @GetMapping("/location")
    public ResponseEntity<List<LocationResponseDTO>> getAllLocation(){
        return ResponseEntity.ok(transitAndStopService.getAllLocations());
    }

    @GetMapping("/location/search")
    public ResponseEntity<List<LocationResponseDTO>> searchLocationByCity(@RequestParam String city){
        return ResponseEntity.ok(transitAndStopService.searchByCity(city));
    }

}
