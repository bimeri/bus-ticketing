package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.dto.CreatePersonalAgencyDTO;
import net.gowaka.gowaka.dto.PersonalAgencyDTO;
import net.gowaka.gowaka.service.PersonalAgencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 8:19 PM <br/>
 */
@RestController
@RequestMapping("/api")
public class PersonalAgencyController {

    private PersonalAgencyService personalAgencyService;

    @Autowired
    public PersonalAgencyController(PersonalAgencyService personalAgencyService) {
        this.personalAgencyService = personalAgencyService;
    }

    @PreAuthorize("hasRole('ROLE_USERS')")
    @PostMapping("/protected/users/agency")
    ResponseEntity<PersonalAgencyDTO> createPersonalAgency(@RequestBody CreatePersonalAgencyDTO createPersonalAgencyDTO){
        return ResponseEntity.ok(personalAgencyService.createPersonalAgency(createPersonalAgencyDTO));
    }

}
