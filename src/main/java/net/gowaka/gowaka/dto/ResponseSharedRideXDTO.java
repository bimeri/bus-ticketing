package net.gowaka.gowaka.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 *@author Nnouka Stephen
 *@date: 04 Oct 2019
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ResponseSharedRideXDTO extends ResponseSharedRideDTO {
    LocalDateTime timestamp;
    Boolean isOfficialAgencyIndicator;
    String personalAgency;
}
