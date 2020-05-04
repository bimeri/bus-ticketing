package net.gowaka.gowaka.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @author Nnouka Stephen
 * @date 14 Oct 2019
 */
@Data
public class JourneyDTO {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Future(message = "departureTime must be in the future")
    private Date departureTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Future(message = "estimatedArrivalTime must be in the future")
    private Date estimatedArrivalTime;
    private DriverDTO driver;
    @NotNull(message = "departureLocation is required")
    private Long departureLocation;
    @NotNull(message = "destination is required")
    private AddStopDTO destination;
    private List<AddStopDTO> transitAndStops;
}
