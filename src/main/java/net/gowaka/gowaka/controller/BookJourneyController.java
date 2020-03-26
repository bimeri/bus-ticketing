package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.dto.BookJourneyRequest;
import net.gowaka.gowaka.dto.PaymentUrlDTO;
import net.gowaka.gowaka.service.BookJourneyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 9:31 AM <br/>
 */
@RestController
@RequestMapping("/api")
public class BookJourneyController {

    private BookJourneyService bookJourneyService;

    @Autowired
    public BookJourneyController(BookJourneyService bookJourneyService) {
        this.bookJourneyService = bookJourneyService;
    }

    @PreAuthorize("hasRole('ROLE_USERS')")
    @PostMapping("/protected/bookJourney/{journeyId}")
    ResponseEntity<PaymentUrlDTO> bookJourney(@PathVariable("journeyId") Long journeyId, @Valid @RequestBody BookJourneyRequest bookJourneyRequest) {
        return ResponseEntity.ok(bookJourneyService.bookJourney(journeyId, bookJourneyRequest));
    }

}
