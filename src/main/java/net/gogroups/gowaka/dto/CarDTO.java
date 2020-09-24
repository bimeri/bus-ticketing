package net.gogroups.gowaka.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/5/19 7:02 PM <br/>
 */
@Data
public class CarDTO {

    private Long id;
    private String name;
    private String licensePlateNumber;
    private Boolean isOfficialAgencyIndicator;
    private Boolean isCarApproved;
    private LocalDateTime timestamp;

}
