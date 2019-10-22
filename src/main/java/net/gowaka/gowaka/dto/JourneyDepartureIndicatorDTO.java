package net.gowaka.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 *@author Nnouka Stephen
 *
 *@date: 22 Oct 2019
 *
 */
@Data
public class JourneyDepartureIndicatorDTO {
    @NotNull(message = "departureIndicator is required")
    private Boolean departureIndicator;
}
