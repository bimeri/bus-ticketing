package net.gowaka.gowaka.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *@author Nnouka Stephen
 *@date: 26 Sep 2019
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ResponseSharedRideDTO extends SharedRideDTO {
    private Long id;
}
