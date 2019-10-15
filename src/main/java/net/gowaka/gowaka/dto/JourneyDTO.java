package net.gowaka.gowaka.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

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
    private Date departureTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date estimatedArrivalTime;
    private DriverDTO driver;
    @NotNull(message = "departureLocation is required")
    private Long departureLocation;
    @NotNull(message = "destination is required")
    private Long destination;
    private List<Long> transitAndStops;
}
