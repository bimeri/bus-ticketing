package net.gogroups.gowaka.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author Nnouka Stephen
 * @date 14 Oct 2019
 */
@Data
public class JourneyResponseDTO {
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date departureTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date estimatedArrivalTime;
    private Boolean departureIndicator;
    private Boolean arrivalIndicator;
    private double amount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date timestamp;
    private DriverDTO driver;
    private LocationResponseDTO departureLocation;
    private LocationStopResponseDTO destination;
    private List<LocationStopResponseDTO> transitAndStops;
    private CarResponseDTO car;
}
