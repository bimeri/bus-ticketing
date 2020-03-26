package net.gowaka.gowaka.network.api.payamgo.service;

import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestDTO;
import net.gowaka.gowaka.service.PayAmGoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;


/**
 * Author: Edward Tanko <br/>
 * Date: 10/5/19 11:01 PM <br/>
 */
@RunWith(MockitoJUnitRunner.class)
public class PayAmGoServiceImplTest {

    @Mock
    private PayAmGoRestClient mockPayAmGoRestClient;
    private PayAmGoService payAmGoService;

    @Before
    public void setUp() {
        payAmGoService = new PayAmGoServiceImpl(mockPayAmGoRestClient);
    }

    @Test
    public void initiatePayment_calls_RestTemplate_with_proper_params() {
        PayAmGoRequestDTO requestResponseDTO = new PayAmGoRequestDTO();
        requestResponseDTO.setAmount("100.00");
        payAmGoService.initiatePayment(requestResponseDTO);
        verify(mockPayAmGoRestClient).initiatePayment(requestResponseDTO);
    }

}