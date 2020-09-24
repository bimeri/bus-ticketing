package net.gogroups.gowaka.domain.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 5:59 AM <br/>
 */
@Data
@Entity
public class SharedRide extends Car{

    private String carOwnerName;
    private String carOwnerIdNumber;
    private String frontPictureOfIdCard;
    private String backPictureOfIdCard;

    @ManyToOne
    private PersonalAgency personalAgency;

}
