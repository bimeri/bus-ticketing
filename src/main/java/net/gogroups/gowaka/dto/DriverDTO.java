package net.gogroups.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author Nnouka Stephen
 * @date 14 Oct 2019
 */
@Data
public class DriverDTO {

    @NotBlank(message = "driver's name is required.")
    @Size(max = 50, message = "Driver's name can not be more than 50 characters")
    private String driverName;

    @NotBlank(message = "driver's license is required.")
    @Size(max = 50, message = "Driver's license can not be more than 50 characters")
    private String driverLicenseNumber;
}
