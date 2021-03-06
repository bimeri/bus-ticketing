package net.gogroups.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 *@author Nnouka Stephen
 *@date: 26 Sep 2019
 *
 */
@Data
public class SharedRideDTO {
    @NotBlank(message = "name is required")
    private String name;
    @NotBlank(message = "licensePlateNumber is required")
    private String licensePlateNumber;
    @NotBlank(message = "carOwnerName is required")
    private String carOwnerName;
    @NotBlank(message = "carOwnerId is required")
    private String carOwnerIdNumber;
}
