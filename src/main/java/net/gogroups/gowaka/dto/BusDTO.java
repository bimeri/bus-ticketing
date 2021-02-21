package net.gogroups.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *@author Nnouka Stephen
 *@date: 26 Sep 2019
 *
 */
@Data
public class BusDTO {

    @NotBlank(message = "bus name is required")
    @Size(max = 50, message = "Bus name must be less than 50 characters")
    private String name;

    @NotBlank(message = "licensePlateNumber is required")
    @Size(max = 10, message = "license number can not be more than 10 characters")
    private String licensePlateNumber;

    @NotNull(message = "numberOfSeats is required")
    @Max(value = 100, message = "seat number can not be more than 100")
    private Integer numberOfSeats;

    @NotNull(message = "seatStructure is required")
    private Long seatStructureId;
}
