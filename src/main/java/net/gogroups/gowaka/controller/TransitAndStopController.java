package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.dto.LocationDTO;
import net.gogroups.gowaka.service.TransitAndStopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;

/**
 * @author Nnouka Stephen
 * @date 26 Sep 2019
 */
@RestController
@RequestMapping("/api/protected")
public class TransitAndStopController {

    private TransitAndStopService transitAndStopService;
    @Autowired
    public TransitAndStopController(TransitAndStopService transitAndStopService) {
        this.transitAndStopService = transitAndStopService;
    }


    @PreAuthorize("hasRole('ROLE_GW_ADMIN')")
    @PostMapping("/location")
    public ResponseEntity addLocation(@Valid @RequestBody LocationDTO locationDTO){
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequestUri()
                .build(transitAndStopService.addLocation(locationDTO).getId())).build();
    }

    @PreAuthorize("hasRole('ROLE_GW_ADMIN')")
    @PostMapping("/location/{id}")
    public ResponseEntity updateLocation(@PathVariable String id, @Valid @RequestBody LocationDTO locationDTO){
        transitAndStopService.updateLocation(Long.parseLong(id), locationDTO);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ROLE_GW_ADMIN')")
    @DeleteMapping("/location/{id}")
    public ResponseEntity deleteLocation(@PathVariable String id){
        transitAndStopService.deleteLocation(Long.parseLong(id));
        return ResponseEntity.noContent().build();
    }
}
