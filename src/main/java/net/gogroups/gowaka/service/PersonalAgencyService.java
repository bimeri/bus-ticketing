package net.gogroups.gowaka.service;

import net.gogroups.gowaka.dto.CreatePersonalAgencyDTO;
import net.gogroups.gowaka.dto.PersonalAgencyDTO;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/21/19 10:51 AM <br/>
 */
public interface PersonalAgencyService {
    PersonalAgencyDTO createPersonalAgency(CreatePersonalAgencyDTO createPersonalAgencyDTO);
}
