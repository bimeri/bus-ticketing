package net.gogroups.gowaka.dto;

import lombok.Data;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 8:31 PM <br/>
 */
@Data
public class OfficialAgencyDTO {

        private Long id;
        private String agencyName;
        private String agencyRegistrationNumber;
        private OfficialAgencyAdminUserDTO agencyAdmin;

}
