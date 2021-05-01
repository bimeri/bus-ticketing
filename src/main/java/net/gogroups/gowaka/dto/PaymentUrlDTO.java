package net.gogroups.gowaka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 9:29 AM <br/>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentUrlDTO {

    private String paymentUrl;
    private Long bookedJourneyId;

}
