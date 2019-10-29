package net.gowaka.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Nnouka Stephen
 * @date 17 Oct 2019
 */
@Data
public class AddStopDTO {
    @NotNull(message = "transitAndStopId is required")
    private Long transitAndStopId;
    private double amount;
}
