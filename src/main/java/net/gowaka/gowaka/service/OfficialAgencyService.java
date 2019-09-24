package net.gowaka.gowaka.service;

import net.gowaka.gowaka.dto.*;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 8:28 PM <br/>
 */
public interface OfficialAgencyService {

    OfficialAgencyDTO createOfficialAgency(CreateOfficialAgencyDTO createOfficialAgencyDTO);
    OfficialAgencyUserDTO assignAgencyUserRole(OfficialAgencyUserRoleRequestDTO officialAgencyUserRoleRequestDTO);
    List<OfficialAgencyUserDTO> getAgencyUsers();
    OfficialAgencyUserDTO addAgencyUser(EmailDTO emailDTO);
    void removeAgencyUser(String userId);
}
