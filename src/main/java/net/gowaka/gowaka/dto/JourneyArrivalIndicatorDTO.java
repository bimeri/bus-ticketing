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
public class JourneyArrivalIndicatorDTO {
    @NotNull(message = " arrivalIndicator is required")
    private Boolean arrivalIndicator;
}
