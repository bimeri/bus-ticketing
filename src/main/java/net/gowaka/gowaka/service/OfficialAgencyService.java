package net.gowaka.gowaka.service;

import net.gowaka.gowaka.dto.CreateOfficialAgencyDTO;
import net.gowaka.gowaka.dto.OfficialAgencyDTO;
import net.gowaka.gowaka.dto.OfficialAgencyUserDTO;
import net.gowaka.gowaka.dto.OfficialAgencyUserRoleRequestDTO;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 8:28 PM <br/>
 */
public interface OfficialAgencyService {

    OfficialAgencyDTO createOfficialAgency(CreateOfficialAgencyDTO createOfficialAgencyDTO);
    OfficialAgencyUserDTO assignAgencyUserRole(OfficialAgencyUserRoleRequestDTO officialAgencyUserRoleRequestDTO);

}
