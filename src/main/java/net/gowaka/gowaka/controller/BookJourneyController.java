package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.domain.service.HtmlToPdfGenarator;
import net.gowaka.gowaka.dto.*;
import net.gowaka.gowaka.service.BookJourneyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.File;
import java.nio.file.Files;
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

    @PreAuthorize("hasRole('ROLE_USERS')")
    @GetMapping("/protected/bookJourney/{bookedJourneyId}/receipt")
    ResponseEntity<byte[]> downloadReceipt(@PathVariable("bookedJourneyId") Long bookedJourneyId) throws Exception {
        String htmlReceipt = bookJourneyService.getHtmlReceipt(bookedJourneyId);
        String filename = "GowakaReceipt_"+new Date();
        File pdfFIle = htmlToPdfGenarator.createPdf(htmlReceipt, filename);

        byte[] contents = Files.readAllBytes(Paths.get(pdfFIle.getAbsolutePath()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(filename, filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        ResponseEntity<byte[]> response = new ResponseEntity<>(contents, headers, HttpStatus.OK);
        return response;
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
    public ResponseEntity checkInPassengerByCode(@RequestBody CodeDTO dto) {
        bookJourneyService.checkInPassengerByCode(dto.getCode());
        return ResponseEntity.noContent().build();
    }


}
