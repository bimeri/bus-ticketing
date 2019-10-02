package net.gowaka.gowaka.service;

import net.gowaka.gowaka.dto.*;

/**
 * @author Nnouka Stephen
 * @date 26 Sep 2019
 */
public interface CarService {
    void approve(ApproveCarDTO approveCarDTO, Long id);
    ResponseBusDTO addOfficialAgencyBus(BusDTO busDTO);
    ResponseSharedRideDTO addSharedRide(SharedRideDTO sharedRideDTO);
}
