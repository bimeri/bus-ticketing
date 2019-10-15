package net.gowaka.gowaka.dto;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private Date departureTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private Date estimatedArrivalTime;
    private Boolean departureIndicator;
    private Boolean arrivalIndicator;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private Date timestamp;
    private DriverDTO driver;
    private LocationResponseDTO departureLocation;
    private LocationResponseDTO destination;
    private List<LocationResponseDTO> transitAndStops;
    private CarDTO car;
}
