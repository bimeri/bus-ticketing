package net.gowaka.gowaka.service;

import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestDTO;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestResponseDTO;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/5/19 9:28 PM <br/>
 */
public interface PayAmGoService {

    PayAmGoRequestResponseDTO initiatePayment(PayAmGoRequestDTO requestResponseDTO);
}
