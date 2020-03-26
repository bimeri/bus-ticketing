package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.dto.BookJourneyRequest;
import net.gowaka.gowaka.dto.BookedJourneyStatusDTO;
import net.gowaka.gowaka.dto.PaymentStatusResponseDTO;
import net.gowaka.gowaka.dto.PaymentUrlDTO;
import net.gowaka.gowaka.service.BookJourneyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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
    @PostMapping("/protected/bookJourney/journey/{journeyId}")
    ResponseEntity<PaymentUrlDTO> bookJourney(@PathVariable("journeyId") Long journeyId, @Valid @RequestBody BookJourneyRequest bookJourneyRequest) {
        return ResponseEntity.ok(bookJourneyService.bookJourney(journeyId, bookJourneyRequest));
    }

    @PreAuthorize("hasRole('ROLE_USERS')")
    @GetMapping("/protected/bookJourney/journey/{journeyId}/booked_seats")
    ResponseEntity<List<Integer>> getAllBookedSeats(@PathVariable("journeyId") Long bookedJourneyId) {
        return ResponseEntity.ok(bookJourneyService.getAllBookedSeats(bookedJourneyId));
    }

    @PreAuthorize("hasRole('ROLE_USERS')")
    @GetMapping("/protected/bookJourney/{bookedJourneyId}")
    ResponseEntity<BookedJourneyStatusDTO> getBookJourneyStatus(@PathVariable("bookedJourneyId") Long bookedJourneyId) {
        return ResponseEntity.ok(bookJourneyService.getBookJourneyStatus(bookedJourneyId));
    }

    @PostMapping("/public/booking/status/{bookedJourneyId}")
    ResponseEntity<?> handlePaymentResponse(@PathVariable("bookedJourneyId") Long bookedJourneyId, @RequestBody PaymentStatusResponseDTO paymentStatusResponseDTO) {
        bookJourneyService.handlePaymentResponse(bookedJourneyId, paymentStatusResponseDTO);
        return ResponseEntity.noContent().build();
    }


}
