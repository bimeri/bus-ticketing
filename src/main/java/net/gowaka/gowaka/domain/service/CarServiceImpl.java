package net.gowaka.gowaka.domain.service;


import net.gowaka.gowaka.domain.model.*;
import net.gowaka.gowaka.domain.repository.CarRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.*;
import net.gowaka.gowaka.exception.ApiException;
import net.gowaka.gowaka.exception.ErrorCodes;
import net.gowaka.gowaka.service.CarService;
import net.gowaka.gowaka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Nnouka Stephen
 * @date 26 Sep 2019
 */
@Service
public class CarServiceImpl implements CarService {
    private CarRepository carRepository;
    private UserService userService;
    private UserRepository userRepository;

    @Autowired
    public CarServiceImpl(CarRepository carRepository, UserService userService, UserRepository userRepository) {
        this.carRepository = carRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public ResponseBusDTO addOfficialAgencyBus(BusDTO busDTO) {
        User agencyManager = verifyCurrentAuthUser();
        // save the bus
        Bus bus = new Bus();
        bus.setName(busDTO.getName());
        bus.setLicensePlateNumber(busDTO.getLicensePlateNumber());
        bus.setNumberOfSeats(busDTO.getNumberOfSeats());
        bus.setTimestamp(LocalDateTime.now());
        bus.setIsOfficialAgencyIndicator(true);
        bus.setIsCarApproved(true);
        bus.setOfficialAgency(agencyManager.getOfficialAgency());
        for (int i = 0; i < busDTO.getNumberOfSeats(); i++){
            Seat seat = new Seat();
            seat.setSeatNumber(i + 1);
            seat.setBus(bus);
            bus.getSeats().add(seat);
        }
        Bus savedbus = carRepository.save(bus);
        ResponseBusDTO responseBusDTO = new ResponseBusDTO();
        responseBusDTO.setId(savedbus.getId());
        responseBusDTO.setNumberOfSeats(savedbus.getNumberOfSeats());
        responseBusDTO.setName(savedbus.getName());
        responseBusDTO.setLicensePlateNumber(savedbus.getLicensePlateNumber());
        return responseBusDTO;
    }

    @Override
    public ResponseSharedRideDTO addSharedRide(SharedRideDTO sharedRideDTO) {
        User user = verifyCurrentAuthUser();
        SharedRide sharedRide = new SharedRide();
        sharedRide.setCarOwnerIdNumber(sharedRideDTO.getCarOwnerIdNumber());
        sharedRide.setCarOwnerName(sharedRideDTO.getCarOwnerName());
        sharedRide.setName(sharedRideDTO.getName());
        sharedRide.setLicensePlateNumber(sharedRideDTO.getLicensePlateNumber());
        sharedRide.setIsCarApproved(false);
        sharedRide.setIsOfficialAgencyIndicator(false);
        sharedRide.setTimestamp(LocalDateTime.now());
        sharedRide.setPersonalAgency(user.getPersonalAgency());
        SharedRide savedRide = carRepository.save(sharedRide);
        ResponseSharedRideDTO responseSharedRideDTO = new ResponseSharedRideDTO();
        responseSharedRideDTO.setId(savedRide.getId());
        responseSharedRideDTO.setName(savedRide.getName());
        responseSharedRideDTO.setLicensePlateNumber(savedRide.getLicensePlateNumber());
        responseSharedRideDTO.setCarOwnerIdNumber(savedRide.getCarOwnerIdNumber());
        responseSharedRideDTO.setCarOwnerName(savedRide.getCarOwnerName());
        return responseSharedRideDTO;
    }

    @Override
    public List<ResponseBusDTO> getAllOfficialAgencyBuses() {
        User agencyManager = verifyCurrentAuthUser();
        System.out.println(agencyManager.getUserId());
        OfficialAgency officialAgency = agencyManager.getOfficialAgency();
        if (officialAgency == null) {
            throw new ApiException("No Agency found", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        if (officialAgency.getBuses().isEmpty()){
            throw new ApiException("Agency is Empty", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NO_CONTENT);
        }
        return officialAgency.getBuses().stream().map(
                officialAgencyBus -> {
                    ResponseBusDTO responseBusDTO = new ResponseBusDTO();
                    responseBusDTO.setId(officialAgencyBus.getId());
                    responseBusDTO.setNumberOfSeats(officialAgencyBus.getNumberOfSeats());
                    responseBusDTO.setLicensePlateNumber(officialAgencyBus.getLicensePlateNumber());
                    responseBusDTO.setName(officialAgencyBus.getName());
                    return responseBusDTO;
                }
        ).collect(Collectors.toList());
    }

    @Override
    public void approve(ApproveCarDTO approveCarDTO, Long id) {
        /*
        Optional<Car> optionalCar = carRepository.findById(id);
        if (!optionalCar.isPresent()){
            throw new ApiException("Car not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Car car = optionalCar.get();
        car.setIsCarApproved(approveCarDTO.isApprove());
        carRepository.save(car);

         */
    }

    private User verifyCurrentAuthUser(){
        UserDTO authUser = userService.getCurrentAuthUser();
        // get user entity
        Optional<User> optionalUser = userRepository.findById(authUser.getId());
        if (!optionalUser.isPresent()){
            throw new ApiException("User not found", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return optionalUser.get();
    }

}
