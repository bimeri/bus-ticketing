package net.gogroups.gowaka.dto;

import lombok.Data;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 8:35 PM <br/>
 */
@Data
public class CreateOfficialAgencyDTO {

    private String agencyName;
    private String agencyRegistrationNumber;
    private String agencyAdminEmail;
    private String policy;

}
