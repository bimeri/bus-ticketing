package net.gogroups.gowaka.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/5/19 7:02 PM <br/>
 */
@Data
public class CarResponseDTO {
    private Long id;
    private String name;
    private String licensePlateNumber;
    private Boolean isOfficialAgencyIndicator;
    private Long agencyId;
    private String agencyName;
    private String agencyLogo;
    private String policy;
    private Boolean isCarApproved;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date timestamp;
    private Number numberOfSeat;
    private String seatStructureCode;

}
