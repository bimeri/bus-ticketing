package net.gowaka.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 *@author Nnouka Stephen
 *@date: 26 Sep 2019
 *
 */
@Data
public class SharedRideDTO {
    private String name;
    @NotNull(message = "license plate number is required")
    private String licensePlateNumber;
    @NotNull(message = "car owner name is required")
    private String carOwnerName;
    @NotNull(message = "car owner ID is required")
    private String carOwnerIdNumber;
}
