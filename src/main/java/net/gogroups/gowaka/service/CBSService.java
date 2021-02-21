package net.gogroups.gowaka.service;

import net.gogroups.gowaka.network.api.cbs.model.CBSBenefitDTO;
import net.gogroups.gowaka.network.api.cbs.model.CBSRewardPointDTO;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 6/21/20 4:59 PM <br/>
 */
public interface CBSService {

    List<CBSBenefitDTO> getAllUserAvailableBenefit(String accessToken);

    CBSRewardPointDTO getUserRewardPoints(String accessToken);

}
