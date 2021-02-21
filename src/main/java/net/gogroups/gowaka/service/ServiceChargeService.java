package net.gogroups.gowaka.service;

import net.gogroups.gowaka.dto.ServiceChargeDTO;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/31/20 6:02 AM <br/>
 */
public interface ServiceChargeService {

    List<ServiceChargeDTO> getServiceCharges();

    void updateServiceCharge(ServiceChargeDTO serviceChargeDTO);

}
