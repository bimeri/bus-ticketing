package net.gowaka.gowaka.service;

import net.gowaka.gowaka.dto.*;

import java.util.List;

/**
 * @author Nnouka Stephen
 * @date 26 Sep 2019
 */
public interface CarService {
    void approve(ApproveCarDTO approveCarDTO, Long id);
    ResponseBusDTO addOfficialAgencyBus(BusDTO busDTO);
    ResponseSharedRideDTO addSharedRide(SharedRideDTO sharedRideDTO);
    List<ResponseBusDTO> getAllOfficialAgencyBuses();
    List<ResponseSharedRideDTO> getAllSharedRides();
    List<CarDTO> getAllUnapprovedCars();
    ResponseCarDTO searchByLicensePlateNumber(String licensePlateNumber);
}
