package net.gogroups.gowaka.controller.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.gogroups.gowaka.domain.model.*;
import net.gogroups.gowaka.domain.repository.*;
import net.gogroups.gowaka.dto.RequestRefundDTO;
import net.gogroups.gowaka.dto.ResponseRefundDTO;
import net.gogroups.security.utils.ApiSecurityTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Author: Edward Tanko <br/>
 * Date: 12/20/20 8:59 AM <br/>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class RefundControllerIntegrationTest {

    @Autowired
    private RefundPaymentTransactionRepository refundPaymentTransactionRepository;
    @Autowired
    private OfficialAgencyRepository officialAgencyRepository;
    @Autowired
    private CarRepository carRepository;
    @Autowired
    private JourneyRepository journeyRepository;
    @Autowired
    private BookedJourneyRepository bookedJourneyRepository;
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Value("${security.jwt.token.privateKey}")
    private String secretKey = "";


    private String jwtToken;
    private Long transactionId;
    private Long journeyId;
    private Long bookedJourneyId;
    private PaymentTransaction paymentTransaction;


    @BeforeEach
    @Transactional
     void setUp() throws Exception {


        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("GG Express");
        OfficialAgency savedOfficialAgency = officialAgencyRepository.save(officialAgency);

        User user = new User();
        user.setUserId("12");
        user.setFullName("GW User");
        user.setEmail("gwuser@gg.com");
        user.setOfficialAgency(savedOfficialAgency);
        User savedUser = userRepository.save(user);

        Bus bus = new Bus();
        bus.setOfficialAgency(savedOfficialAgency);
        Bus savedCar = carRepository.save(bus);

        Journey journey = new Journey();
        journey.setCar(savedCar);
        Journey savedJourney = journeyRepository.save(journey);

        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(savedJourney);
        bookedJourney.setUser(savedUser);
        BookedJourney savedBookedJourney = bookedJourneyRepository.save(bookedJourney);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setBookedJourney(savedBookedJourney);
        paymentTransaction.setAmount(1100.00);
        paymentTransaction.setAgencyAmount(1000.00);
        paymentTransaction.setServiceChargeAmount(100.00);
        paymentTransaction.setTransactionStatus("COMPLETED");
        PaymentTransaction savedPaymentTransaction = paymentTransactionRepository.save(paymentTransaction);

        transactionId = savedPaymentTransaction.getId();
        journeyId = savedJourney.getId();
        bookedJourneyId = savedBookedJourney.getId();
        this.paymentTransaction = savedPaymentTransaction;
        jwtToken = ApiSecurityTestUtils.createToken("12", "gwuser@gg.com", "GW User", secretKey, new String[]{"AGENCY_MANAGER", "USERS"});
    }

    @Test
    @Transactional
    void requestRefund_success_return_200() throws Exception {

        RequestRefundDTO requestRefundDTO = new RequestRefundDTO();
        requestRefundDTO.setMessage("I need my money back!");
        requestRefundDTO.setBookedJourneyId(bookedJourneyId);
        requestRefundDTO.setTransactionId(transactionId);
        RequestBuilder requestBuilder = post("/api/protected/bookedJourneys/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(requestRefundDTO))
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent());
    }

    @Test
    @Transactional
    void getUserRefundRequest_success_return_200() throws Exception {


        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setPaymentTransaction(paymentTransaction);
        refundPaymentTransaction.setRequestedDate(LocalDateTime.now());
        refundPaymentTransaction.setRefundRequestMessage("Please refund");
        RefundPaymentTransaction savedRefundPaymentTransaction = refundPaymentTransactionRepository.save(refundPaymentTransaction);

        paymentTransaction.setRefundPaymentTransaction(savedRefundPaymentTransaction);
        paymentTransactionRepository.save(paymentTransaction);

        RequestBuilder requestBuilder = get("/api/protected/bookedJourneys/refund/{id}/user", savedRefundPaymentTransaction.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    void responseRefund_success_return_200() throws Exception {

        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setPaymentTransaction(paymentTransaction);
        refundPaymentTransaction.setRequestedDate(LocalDateTime.now());
        refundPaymentTransaction.setRefundRequestMessage("Please refund");
        RefundPaymentTransaction savedRefundPaymentTransaction = refundPaymentTransactionRepository.save(refundPaymentTransaction);

        paymentTransaction.setRefundPaymentTransaction(savedRefundPaymentTransaction);
        paymentTransactionRepository.save(paymentTransaction);

        ResponseRefundDTO responseRefundDTO = new ResponseRefundDTO();
        responseRefundDTO.setIsRefundApproved(true);
        responseRefundDTO.setMessage("I approve this");
        responseRefundDTO.setAmount(1000.00);
        RequestBuilder requestBuilder = post("/api/protected/bookedJourneys/refund/{id}", savedRefundPaymentTransaction.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(responseRefundDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent());
    }

    @Test
    @Transactional
    void getAllJourneyRefunds_success_return_200() throws Exception {

        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setPaymentTransaction(paymentTransaction);
        refundPaymentTransaction.setRequestedDate(LocalDateTime.now());
        refundPaymentTransaction.setRefundRequestMessage("Please refund");
        RefundPaymentTransaction savedRefundPaymentTransaction = refundPaymentTransactionRepository.save(refundPaymentTransaction);

        paymentTransaction.setRefundPaymentTransaction(savedRefundPaymentTransaction);
        paymentTransactionRepository.save(paymentTransaction);

        RequestBuilder requestBuilder = get("/api/protected/bookedJourneys/journey/{id}/refund", journeyId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    void refunded_success_return_200() throws Exception {

        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setPaymentTransaction(paymentTransaction);
        refundPaymentTransaction.setRequestedDate(LocalDateTime.now());
        refundPaymentTransaction.setRefundRequestMessage("Please refund");
        RefundPaymentTransaction savedRefundPaymentTransaction = refundPaymentTransactionRepository.save(refundPaymentTransaction);

        paymentTransaction.setRefundPaymentTransaction(savedRefundPaymentTransaction);
        paymentTransactionRepository.save(paymentTransaction);

        RequestBuilder requestBuilder = post("/api/protected/bookedJourneys/refund/{id}/refunded", savedRefundPaymentTransaction.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent());
    }

}
