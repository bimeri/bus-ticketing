package net.gowaka.gowaka.domain;

import lombok.Data;

import javax.persistence.Embeddable;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 6:00 AM <br/>
 */
@Data
@Embeddable
public class Driver {
    private String driverName;
    private String driverLicenseNumber;
    private String frontPictureDriverLicense;
    private String backPictureDriverLicense;
}
