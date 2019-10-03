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
        OfficialAgency officialAgency = getOfficialAgency(verifyCurrentAuthUser());
        String licensePlateNumber = busDTO.getLicensePlateNumber();
        verifyCarExists(licensePlateNumber);
        // save the bus
        Bus bus = new Bus();
        bus.setName(busDTO.getName());
        bus.setLicensePlateNumber(
                licensePlateNumber == null ? null : licensePlateNumber.trim()
        );
        bus.setNumberOfSeats(busDTO.getNumberOfSeats());
        bus.setTimestamp(LocalDateTime.now());
        bus.setIsOfficialAgencyIndicator(true);
        bus.setIsCarApproved(true);
        bus.setOfficialAgency(officialAgency);
        if (busDTO.getNumberOfSeats() != null){
            for (int i = 0; i < busDTO.getNumberOfSeats(); i++){
                Seat seat = new Seat();
                seat.setSeatNumber(i + 1);
                seat.setBus(bus);
                bus.getSeats().add(seat);
            }
        }
        Bus savedbus = carRepository.save(bus);
        return getResponseBusDTO(savedbus);
    }

    @Override
    public ResponseSharedRideDTO addSharedRide(SharedRideDTO sharedRideDTO) {
        PersonalAgency personalAgency = getPersonalAgency(verifyCurrentAuthUser());
        String licensePlateNumber = sharedRideDTO.getLicensePlateNumber();
        verifyCarExists(licensePlateNumber);
        SharedRide sharedRide = new SharedRide();
        sharedRide.setCarOwnerIdNumber(sharedRideDTO.getCarOwnerIdNumber());
        sharedRide.setCarOwnerName(sharedRideDTO.getCarOwnerName());
        sharedRide.setName(sharedRideDTO.getName());
        sharedRide.setLicensePlateNumber(
                licensePlateNumber == null ? null : licensePlateNumber.trim());
        sharedRide.setIsCarApproved(false);
        sharedRide.setIsOfficialAgencyIndicator(false);
        sharedRide.setTimestamp(LocalDateTime.now());
        sharedRide.setPersonalAgency(personalAgency);
        SharedRide savedRide = carRepository.save(sharedRide);
        return getResponseSharedRideDTO(savedRide);
    }

    @Override
    public List<ResponseBusDTO> getAllOfficialAgencyBuses() {
        return getBuses(getOfficialAgency(verifyCurrentAuthUser())).stream().map(
                this::getResponseBusDTO
        ).collect(Collectors.toList());
    }

    @Override
    public List<ResponseSharedRideDTO> getAllSharedRides() {
        return getSharedRides(getPersonalAgency(verifyCurrentAuthUser())).stream().map(
                this::getResponseSharedRideDTO
        ).collect(Collectors.toList());
    }

    @Override
    public void approve(ApproveCarDTO approveCarDTO, Long id) {
        Optional<Car> optionalCar = carRepository.findById(id);
        if (!optionalCar.isPresent()){
            throw new ApiException("Car not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        Car car = optionalCar.get();
        car.setIsCarApproved(approveCarDTO.isApprove());
        carRepository.save(car);
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

    private PersonalAgency getPersonalAgency(User user) {
        PersonalAgency personalAgency = user.getPersonalAgency();
        if (personalAgency == null) {
            throw new ApiException("No Personal Agency found for this user", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return personalAgency;
    }

    private OfficialAgency getOfficialAgency(User user) {
        OfficialAgency officialAgency = user.getOfficialAgency();
        if (officialAgency == null){
            throw new ApiException("No Official Agency found for this user", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return officialAgency;
    }

    private ResponseSharedRideDTO getResponseSharedRideDTO(SharedRide sharedRide) {
        ResponseSharedRideDTO responseSharedRideDTO = new ResponseSharedRideDTO();
        responseSharedRideDTO.setId(sharedRide.getId());
        responseSharedRideDTO.setName(sharedRide.getName());
        responseSharedRideDTO.setLicensePlateNumber(sharedRide.getLicensePlateNumber());
        responseSharedRideDTO.setCarOwnerIdNumber(sharedRide.getCarOwnerIdNumber());
        responseSharedRideDTO.setCarOwnerName(sharedRide.getCarOwnerName());
        return responseSharedRideDTO;
    }

    private ResponseBusDTO getResponseBusDTO(Bus bus){
        ResponseBusDTO responseBusDTO = new ResponseBusDTO();
        responseBusDTO.setId(bus.getId());
        responseBusDTO.setNumberOfSeats(bus.getNumberOfSeats());
        responseBusDTO.setName(bus.getName());
        responseBusDTO.setLicensePlateNumber(bus.getLicensePlateNumber());
        return responseBusDTO;
    }

    private List<Bus> getBuses(OfficialAgency officialAgency){
        List<Bus> buses = officialAgency.getBuses();
        if (buses.isEmpty()){
            throw new ApiException("Agency is Empty", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NO_CONTENT);
        }
        return buses;
    }

    private List<SharedRide> getSharedRides(PersonalAgency personalAgency){
        List<SharedRide> sharedRides = personalAgency.getSharedRides();
        if (sharedRides.isEmpty()){
            throw new ApiException("Agency is Empty", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NO_CONTENT);
        }
        return sharedRides;
    }

    private void verifyCarExists(String licensePlateNumber){
        if (licensePlateNumber != null && carRepository.findByLicensePlateNumber(licensePlateNumber.trim()).isPresent()){
            throw new ApiException("License plate number already in use",
                    ErrorCodes.LICENSE_PLATE_NUMBER_ALREADY_IN_USE.toString(), HttpStatus.CONFLICT);
        }
    }

}
