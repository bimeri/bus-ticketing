package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.domain.service.HtmlToPdfGenarator;
import net.gogroups.gowaka.dto.*;
import net.gogroups.gowaka.service.BookJourneyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.File;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 9:31 AM <br/>
 */
@RestController
@RequestMapping("/api")
public class BookJourneyController {

    private BookJourneyService bookJourneyService;
    private HtmlToPdfGenarator htmlToPdfGenarator;

    @Autowired
    public BookJourneyController(BookJourneyService bookJourneyService, HtmlToPdfGenarator htmlToPdfGenarator) {
        this.bookJourneyService = bookJourneyService;
        this.htmlToPdfGenarator = htmlToPdfGenarator;
    }

    @PreAuthorize("hasRole('ROLE_USERS')")
    @PostMapping("/protected/bookJourney/journey/{journeyId}")
    ResponseEntity<PaymentUrlDTO> bookJourney(@PathVariable("journeyId") Long journeyId, @Valid @Validated @RequestBody BookJourneyRequest bookJourneyRequest) {
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

    @PreAuthorize("hasRole('ROLE_USERS')")
    @GetMapping("/protected/bookJourney/{bookedJourneyId}/receipt")
    ResponseEntity<Resource> downloadReceipt(@PathVariable("bookedJourneyId") Long bookedJourneyId) throws Exception {
        String htmlReceipt = bookJourneyService.getHtmlReceipt(bookedJourneyId);
        String filename = "GowakaReceipt_"+new Date();
        File pdfFIle = htmlToPdfGenarator.createPdf(htmlReceipt, filename);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(filename, filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return new ResponseEntity<>(new UrlResource(Paths.get(pdfFIle.getAbsolutePath()).toUri()), headers, HttpStatus.OK);
    }

    @PostMapping("/public/booking/status/{bookedJourneyId}")
    ResponseEntity<?> handlePaymentResponse(@PathVariable("bookedJourneyId") Long bookedJourneyId, @RequestBody PaymentStatusResponseDTO paymentStatusResponseDTO) {
        bookJourneyService.handlePaymentResponse(bookedJourneyId, paymentStatusResponseDTO);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ROLE_USERS')")
    @GetMapping("/protected/bookJourney/history")
    ResponseEntity<List<BookedJourneyStatusDTO>> bookedJourneyHistory() {
        return ResponseEntity.ok(bookJourneyService.getUserBookedJourneyHistory());
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_CHECKING', 'ROLE_AGENCY_BOOKING')")
    @GetMapping("/protected/checkIn_status")
    public ResponseEntity<OnBoardingInfoDTO> getOnBoardingInfoResponse(@RequestParam("code") String code) {
        return ResponseEntity.ok(bookJourneyService.getPassengerOnBoardingInfo(code));
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_CHECKING', 'ROLE_AGENCY_BOOKING')")
    @PostMapping("/protected/checkIn")
    public ResponseEntity<?> checkInPassengerByCode(@RequestBody CodeDTO dto) {
        bookJourneyService.checkInPassengerByCode(dto.getCode());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_ADMIN', 'ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR', 'ROLE_AGENCY_BOOKING')")
    @GetMapping("/protected/agency/journeys/{journeyId}/booking_history")
    public ResponseEntity<List<OnBoardingInfoDTO>> getAllOnBoardingInfoByJourney(@PathVariable("journeyId") Long journeyId) {
        return ResponseEntity.ok(bookJourneyService.getAllPassengerOnBoardingInfo(journeyId));
    }


}
