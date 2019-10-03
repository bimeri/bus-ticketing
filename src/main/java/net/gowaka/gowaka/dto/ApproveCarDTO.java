package net.gowaka.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Nnouka Stephen
 * @date 26 Sep 2019
 */
@Data
public class ApproveCarDTO {
    @NotNull(message = "approve is required")
    private boolean approve;
}
