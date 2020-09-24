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
public class PaymentUrlDTO {

    private String paymentUrl;

    public PaymentUrlDTO(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }
}
