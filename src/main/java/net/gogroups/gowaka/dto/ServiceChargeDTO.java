package net.gogroups.gowaka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/31/20 6:03 AM <br/>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceChargeDTO {

    private String id; //PLATFORM_SERVICE_CHARGE,SMS_NOTIF,
    private Double percentageCharge;
    private Double flatCharge;

}
