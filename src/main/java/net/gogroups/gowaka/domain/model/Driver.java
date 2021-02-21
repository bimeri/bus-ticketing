package net.gogroups.gowaka.domain.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 6:00 AM <br/>
 */
@Data
@Embeddable
public class Driver {

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "driver_license_number")
    private String driverLicenseNumber;

    private String frontPictureDriverLicense;
    private String backPictureDriverLicense;
}
