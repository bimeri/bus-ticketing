package net.gowaka.gowaka.domain.service;


import net.gowaka.gowaka.domain.model.*;
import net.gowaka.gowaka.domain.repository.CarRepository;
import net.gowaka.gowaka.domain.repository.OfficialAgencyRepository;
import net.gowaka.gowaka.domain.repository.PersonalAgencyRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.ApproveCarDTO;
import net.gowaka.gowaka.dto.BusDTO;
import net.gowaka.gowaka.dto.ResponseBusDTO;
import net.gowaka.gowaka.dto.UserDTO;
import net.gowaka.gowaka.exception.ApiException;
import net.gowaka.gowaka.exception.ErrorCodes;
import net.gowaka.gowaka.service.CarService;
import net.gowaka.gowaka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * @author Nnouka Stephen
 * @date 26 Sep 2019
 */
@Service
public class CarServiceImpl implements CarService {
    private CarRepository carRepository;
    private UserService userService;
    private UserRepository userRepository;
    private static final String  role_prefix = "ROLE_";

    @Autowired
    public CarServiceImpl(CarRepository carRepository, UserService userService, UserRepository userRepository) {
        this.carRepository = carRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public ResponseBusDTO addOfficialAgencyBus(BusDTO busDTO) {
        UserDTO authUser = userService.getCurrentAuthUser();
        String []managementRoles = {role_prefix + "AGENCY_ADMIN", role_prefix + "AGENCY_MANAGER"};
        // verify auth user role
        if (Collections.disjoint(authUser.getRoles(), Arrays.asList(managementRoles))){
            throw new ApiException("You are not allowed to perform this action", ErrorCodes.ACCESS_DENIED.toString(), HttpStatus.UNAUTHORIZED);
        }
        // get user entity
        Optional<User> optionalUser = userRepository.findById(authUser.getId());
        if (!optionalUser.isPresent()){
            throw new ApiException("User not found", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        User agencyManager = optionalUser.get();
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

}
