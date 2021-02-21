package net.gogroups.gowaka.service;

import net.gogroups.gowaka.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 8:28 PM <br/>
 */
public interface OfficialAgencyService {

    OfficialAgencyDTO createOfficialAgency(CreateOfficialAgencyDTO createOfficialAgencyDTO);

    void uploadAgencyLogo(Long agencyId, MultipartFile logoFile);

    List<OfficialAgencyDTO> getAllAgencies();

    OfficialAgencyDTO getUserAgency();

    void updateOfficialAgency(Long agencyId, OfficialAgencyDTO officialAgencyDTO);

    OfficialAgencyUserDTO assignAgencyUserRole(OfficialAgencyUserRoleRequestDTO officialAgencyUserRoleRequestDTO);

    List<OfficialAgencyUserDTO> getAgencyUsers();

    OfficialAgencyUserDTO addAgencyUser(EmailDTO emailDTO);

    void removeAgencyUser(String userId);
}
