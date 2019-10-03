package net.gowaka.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 *@author Nnouka Stephen
 *@date: 26 Sep 2019
 *
 */
@Data
public class BusDTO {
    private String name;
    @NotBlank(message = "licensePlateNumber is required")
    private String licensePlateNumber;
    @NotNull(message = "numberOfSeats is required")
    private Integer numberOfSeats;
}
