package net.gowaka.gowaka.service;

import net.gowaka.gowaka.network.api.cbs.model.CBSBenefitDTO;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 6/21/20 4:59 PM <br/>
 */
public interface CBSService {

    List<CBSBenefitDTO> getAllAvailableBenefit();

    List<CBSBenefitDTO> getAllUserAvailableBenefit(String userId);

}
