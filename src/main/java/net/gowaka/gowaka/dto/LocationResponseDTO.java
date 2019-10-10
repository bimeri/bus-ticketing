package net.gowaka.gowaka.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Nnouka Stephen
 * @date 07 Oct 2019
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LocationResponseDTO extends LocationDTO {
    private Long id;
}
