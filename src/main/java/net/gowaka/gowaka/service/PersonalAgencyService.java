package net.gowaka.gowaka.service;

import net.gowaka.gowaka.dto.CreatePersonalAgencyDTO;
import net.gowaka.gowaka.dto.PersonalAgencyDTO;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/21/19 10:51 AM <br/>
 */
public interface PersonalAgencyService {
    PersonalAgencyDTO createPersonalAgency(CreatePersonalAgencyDTO createPersonalAgencyDTO);
}
