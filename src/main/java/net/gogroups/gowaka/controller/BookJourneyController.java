package net.gogroups.gowaka.controller;

import lombok.extern.slf4j.Slf4j;
import net.gogroups.dto.PaginatedResponse;
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
@Slf4j
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
    ResponseEntity<PaymentUrlDTO> bookJourney(@PathVariable("journeyId") Long journeyId, @Validated @RequestBody BookJourneyRequest bookJourneyRequest) {
        log.info("user booking journey for :{}", journeyId);
        return ResponseEntity.ok(bookJourneyService.bookJourney(journeyId, bookJourneyRequest));
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_ADMIN', 'ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR', 'ROLE_AGENCY_BOOKING')")
    @PostMapping("/protected/bookJourney/journey/{journeyId}/agency")
    ResponseEntity<?> agencyUserBookJourney(@PathVariable("journeyId") Long journeyId, @Validated @RequestBody BookJourneyRequest bookJourneyRequest) {
        log.info("agency booking journey for :{}", journeyId);
        bookJourneyService.agencyUserBookJourney(journeyId, bookJourneyRequest);
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("hasAnyRole('ROLE_AGENCY_ADMIN', 'ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR', 'ROLE_AGENCY_BOOKING')")
    @PostMapping("/protected/bookJourney/find_passenger")
    ResponseEntity<List<GwPassenger>> findPassenger(@Validated @RequestBody SearchPassengerDTO phoneNumberDTO) {
        log.info("searching passenger by phone number :{}", phoneNumberDTO.getPhoneNumber());
        return ResponseEntity.ok(bookJourneyService.searchPassenger(phoneNumberDTO));
    }


    @PreAuthorize("hasRole('ROLE_USERS')")
    @GetMapping("/protected/bookJourney/journey/{journeyId}/booked_seats")
    ResponseEntity<List<Integer>> getAllBookedSeats(@PathVariable("journeyId") Long journeyId) {
        log.info("getting all booked seats for journey  :{}", journeyId);
        return ResponseEntity.ok(bookJourneyService.getAllBookedSeats(journeyId));
    }

    @PreAuthorize("hasRole('ROLE_USERS')")
    @GetMapping("/protected/bookJourney/{bookedJourneyId}")
    ResponseEntity<BookedJourneyStatusDTO> getBookJourneyStatus(@PathVariable("bookedJourneyId") Long bookedJourneyId) {
        return ResponseEntity.ok(bookJourneyService.getBookJourneyStatus(bookedJourneyId, true));
    }

    @PreAuthorize("hasRole('ROLE_USERS')")
    @GetMapping("/protected/bookJourney/{bookedJourneyId}/receipt")
    ResponseEntity<Resource> downloadReceipt(@PathVariable("bookedJourneyId") Long bookedJourneyId) throws Exception {
        return getReceiptResourceResponseEntity(bookedJourneyId, true);
    }

    @GetMapping("/public/bookJourney/{bookedJourneyId}/receipt")
    ResponseEntity<Resource> downloadReceiptPublicRoute(@PathVariable("bookedJourneyId") Long bookedJourneyId) throws Exception {
        return getReceiptResourceResponseEntity(bookedJourneyId, false);
    }

    @PostMapping("/public/booking/status/{bookedJourneyId}")
    ResponseEntity<?> handlePaymentResponse(@PathVariable("bookedJourneyId") Long bookedJourneyId, @RequestBody PaymentStatusResponseDTO paymentStatusResponseDTO) {
        bookJourneyService.handlePaymentResponse(bookedJourneyId, paymentStatusResponseDTO);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ROLE_USERS')")
    @GetMapping("/protected/bookJourney/history")
    ResponseEntity<PaginatedResponse<BookedJourneyStatusDTO>> bookedJourneyHistory(
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(name = "limit", defaultValue = Integer.MAX_VALUE + "", required = false) Integer limit
    ) {
        return ResponseEntity.ok(bookJourneyService.getUserBookedJourneyHistory(pageNumber, limit));
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR', 'ROLE_AGENCY_CHECKING', 'ROLE_AGENCY_BOOKING')")
    @GetMapping("/protected/checkIn_status")
    public ResponseEntity<OnBoardingInfoDTO> getOnBoardingInfoResponse(@RequestParam("code") String code) {
        log.info("getting on boarding info for code :{}", code);
        return ResponseEntity.ok(bookJourneyService.getPassengerOnBoardingInfo(code));
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_MANAGER','ROLE_AGENCY_OPERATOR', 'ROLE_AGENCY_CHECKING', 'ROLE_AGENCY_BOOKING')")
    @PostMapping("/protected/checkIn")
    public ResponseEntity<?> checkInPassengerByCode(@RequestBody @Validated CodeDTO dto) {
        log.info("checking in code :{}", dto.getCode());
        bookJourneyService.checkInPassengerByCode(dto.getCode());
        log.info("checking successful for code :{}", dto.getCode());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR', 'ROLE_AGENCY_BOOKING')")
    @PostMapping("/protected/bookJourney/{bookedJourneyId}/cancel_trip")
    public ResponseEntity<?> cancelBookings(@PathVariable("bookedJourneyId") Long bookJourneyId, @RequestBody @Validated List<@Valid CodeDTO> codes) {
        log.info("cancel booking in codes :{} and id: {}", codes, bookJourneyId);
        bookJourneyService.cancelBookings(bookJourneyId, codes);
        log.info("cancel successful for code :{}and id: {}", codes, bookJourneyId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_AGENCY_ADMIN', 'ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR', 'ROLE_AGENCY_BOOKING')")
    @GetMapping("/protected/agency/journeys/{journeyId}/booking_history")
    public ResponseEntity<List<OnBoardingInfoDTO>> getAllOnBoardingInfoByJourney(@PathVariable("journeyId") Long journeyId) {
        return ResponseEntity.ok(bookJourneyService.getAllPassengerOnBoardingInfo(journeyId));
    }

    @PreAuthorize("hasAnyRole('ROLE_USERS', 'ROLE_AGENCY_ADMIN', 'ROLE_AGENCY_MANAGER', 'ROLE_AGENCY_OPERATOR', 'ROLE_AGENCY_BOOKING')")
    @PostMapping("/protected/bookJourney/{bookJourneyId}/change_seat_number")
    public ResponseEntity<?> changeSeats(@RequestBody List<ChangeSeatDTO> changeSeatList, @PathVariable("bookJourneyId") Long bookJourneyId) {
        bookJourneyService.changeSeatNumber(changeSeatList, bookJourneyId);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Resource> getReceiptResourceResponseEntity(Long bookedJourneyId, boolean isAuth) throws Exception {
        String htmlReceipt = bookJourneyService.getHtmlReceipt(bookedJourneyId, isAuth);
        String filename = "GowakaReceipt_" + new Date().getTime();
        File pdfFIle = htmlToPdfGenarator.createPdf(htmlReceipt, filename);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(filename, filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return new ResponseEntity<>(new UrlResource(Paths.get(pdfFIle.getAbsolutePath()).toUri()), headers, HttpStatus.OK);
    }
}
