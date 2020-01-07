package net.gowaka.gowaka.service;


import net.gowaka.gowaka.dto.*;

import java.util.List;

/**
 * @author Nnouka Stephen
 * @date 26 Sep 2019
 */
public interface CarService {
    void approve(ApproveCarDTO approveCarDTO, Long id);
    BusResponseDTO addOfficialAgencyBus(BusDTO busDTO);
    SharedRideResponseDTO addSharedRide(SharedRideDTO sharedRideDTO);
    List<BusResponseDTO> getAllOfficialAgencyBuses();
    List<SharedRideResponseDTO> getAllSharedRides();
    List<CarDTO> getAllUnapprovedCars();
    CarDTO searchByLicensePlateNumber(String licensePlateNumber);
    void updateAgencyCarInfo(Long busId, BusDTO busDTO);
}
