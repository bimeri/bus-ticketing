package net.gowaka.gowaka.domain;

import lombok.Data;

import javax.persistence.Entity;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 5:59 AM <br/>
 */
@Data
@Entity
public class SharedRide extends Car{

    private String carOwnerName;
    private String getCarOwnerIdNumber;
    private String frontPictureOfIdCard;
    private String backPictureOfIdCard;
}
