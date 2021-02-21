package net.gogroups.gowaka.service;


import net.gogroups.gowaka.dto.*;

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

    BusResponseDTO getOfficialAgencyBuses(Long carId);

    List<SharedRideResponseDTO> getAllSharedRides();

    List<CarDTO> getAllUnapprovedCars();

    CarDTO searchByLicensePlateNumber(String licensePlateNumber);

    void updateAgencyCarInfo(Long carId, BusDTO busDTO);

    void deleteAgencyCarInfo(Long carId);

    List<SeatStructureDTO> getSeatStructures(Integer numberOfSeats);
}
