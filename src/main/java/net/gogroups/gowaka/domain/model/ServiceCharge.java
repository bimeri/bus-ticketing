package net.gogroups.gowaka.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/31/20 6:09 AM <br/>
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCharge extends BaseEntity{

    @Id
    private String id; //PLATFORM_SERVICE_CHARGE,SMS_NOTIF,
    private Double percentageCharge;
    private Double flatCharge;

}
