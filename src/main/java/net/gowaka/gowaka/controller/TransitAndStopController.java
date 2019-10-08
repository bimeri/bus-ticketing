package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.dto.LocationDTO;
import net.gowaka.gowaka.service.TransitAndStopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;

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
}
