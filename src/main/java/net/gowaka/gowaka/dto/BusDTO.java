package net.gowaka.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *@author Nnouka Stephen
 *@date: 26 Sep 2019
 *
 */
@Data
public class BusDTO {
    private String name;
    @NotNull(message = "license plate number is required")
    private String licensePlateNumber;
    @NotNull(message = "number of seats is required")
    private Integer numberOfSeats;
}
