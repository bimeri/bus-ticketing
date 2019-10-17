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
    public BusResponseDTO addOfficialAgencyBus(BusDTO busDTO) {
        OfficialAgency officialAgency = getOfficialAgency(verifyCurrentAuthUser());
        String licensePlateNumber = busDTO.getLicensePlateNumber();
        verifyCarLicensePlateNumber(licensePlateNumber);
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
        return getBusResponseDTO(savedbus);
    }

    @Override
    public SharedRideResponseDTO addSharedRide(SharedRideDTO sharedRideDTO) {
        PersonalAgency personalAgency = getPersonalAgency(verifyCurrentAuthUser());
        String licensePlateNumber = sharedRideDTO.getLicensePlateNumber();
        verifyCarLicensePlateNumber(licensePlateNumber);
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
        return getSharedRideResponseDTO(savedRide);
    }

    @Override
    public List<BusResponseDTO> getAllOfficialAgencyBuses() {
        return getBuses(getOfficialAgency(verifyCurrentAuthUser())).stream().map(
                this::getBusResponseDTO
        ).collect(Collectors.toList());
    }

    @Override
    public List<SharedRideResponseDTO> getAllSharedRides() {
        return getSharedRides(getPersonalAgency(verifyCurrentAuthUser())).stream().map(
                this::getSharedRideResponseDTO
        ).collect(Collectors.toList());
    }

    @Override
    public void approve(ApproveCarDTO approveCarDTO, Long id) {
        Car car = getCarById(id);
        car.setIsCarApproved(approveCarDTO.isApprove());
        carRepository.save(car);
    }

    @Override
    public List<CarDTO> getAllUnapprovedCars() {
        List<Car> unApprovedCars = carRepository.findByIsCarApproved(false);
        List<CarDTO> carDTOs = unApprovedCars.stream()
                .map(car -> {
                    CarDTO carDTO = new CarDTO();
                    carDTO.setId(car.getId());
                    carDTO.setName(car.getName());
                    carDTO.setIsCarApproved(car.getIsCarApproved());
                    carDTO.setIsOfficialAgencyIndicator(car.getIsOfficialAgencyIndicator());
                    carDTO.setLicensePlateNumber(car.getLicensePlateNumber());
                    carDTO.setTimestamp(car.getTimestamp());
                    return carDTO;
                }).collect(Collectors.toList());

       return carDTOs;
    }

    @Override
    public CarDTO searchByLicensePlateNumber(String licensePlateNumber) {
        return getCarDTO(getCarByLicensePlateNumber(licensePlateNumber));
    }

    /**
     * verify and return the current user in cases where user id is relevant
     * @return user
     */
    private User verifyCurrentAuthUser(){
        UserDTO authUser = userService.getCurrentAuthUser();
        // get user entity
        Optional<User> optionalUser = userRepository.findById(authUser.getId());
        if (!optionalUser.isPresent()){
            throw new ApiException("User not found", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return optionalUser.get();
    }

    /**
     * Get the personal agency of user or throw api exception if agency is not found
     * @param user
     * @return personalAgency
     */
    private PersonalAgency getPersonalAgency(User user) {
        PersonalAgency personalAgency = user.getPersonalAgency();
        if (personalAgency == null) {
            throw new ApiException("No Personal Agency found for this user", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return personalAgency;
    }

    /**
     * Get the personal agency of user or throw api exception if agency is not found
     * @param user
     * @return officialAgency
     */
    private OfficialAgency getOfficialAgency(User user) {
        OfficialAgency officialAgency = user.getOfficialAgency();
        if (officialAgency == null){
            throw new ApiException("No Official Agency found for this user", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return officialAgency;
    }

    /**
     * Get buses of official agency or throw api exception if agency is empty
     * @param officialAgency
     * @return List: bus
     */
    private List<Bus> getBuses(OfficialAgency officialAgency){
        List<Bus> buses = officialAgency.getBuses();
        if (buses.isEmpty()){
            throw new ApiException("Agency is Empty", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NO_CONTENT);
        }
        return buses;
    }

    /**
     * Get sharedRides of Agency or throw api exception if agency is empty
     * @param personalAgency
     * @return sharedRides
     */
    private List<SharedRide> getSharedRides(PersonalAgency personalAgency){
        List<SharedRide> sharedRides = personalAgency.getSharedRides();
        if (sharedRides.isEmpty()){
            throw new ApiException("Agency is Empty", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NO_CONTENT);
        }
        return sharedRides;
    }

    /**
     * throws license plate already in use api exception
     * @param licensePlateNumber
     */
    private void verifyCarLicensePlateNumber(String licensePlateNumber){
        if (licensePlateNumber != null && carRepository.findByLicensePlateNumberIgnoreCase(licensePlateNumber.trim()).isPresent()){
            throw new ApiException("License plate number already in use",
                    ErrorCodes.LICENSE_PLATE_NUMBER_ALREADY_IN_USE.toString(), HttpStatus.CONFLICT);
        }
    }

    /**
     *
     * @param id
     * @return Car
     */
    private Car getCarById(Long id){
        Optional<Car> optionalCar = carRepository.findById(id);
        if (!optionalCar.isPresent()){
            throw new ApiException("Car not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return optionalCar.get();
    }
    private Car getCarByLicensePlateNumber(String licensePlateNumber){
        Optional<Car> optionalCar = carRepository.findByLicensePlateNumberIgnoreCase(licensePlateNumber);
        if (!optionalCar.isPresent()){
            throw new ApiException("Car not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return optionalCar.get();
    }

    private SharedRideResponseDTO getSharedRideResponseDTO(SharedRide sharedRide) {
        SharedRideResponseDTO sharedRideResponseDTO = new SharedRideResponseDTO();
        sharedRideResponseDTO.setId(sharedRide.getId());
        sharedRideResponseDTO.setName(sharedRide.getName());
        sharedRideResponseDTO.setLicensePlateNumber(sharedRide.getLicensePlateNumber());
        sharedRideResponseDTO.setCarOwnerIdNumber(sharedRide.getCarOwnerIdNumber());
        sharedRideResponseDTO.setCarOwnerName(sharedRide.getCarOwnerName());
        sharedRideResponseDTO.setIsCarApproved(sharedRide.getIsCarApproved());
        return sharedRideResponseDTO;
    }

    private BusResponseDTO getBusResponseDTO(Bus bus){
        BusResponseDTO busResponseDTO = new BusResponseDTO();
        busResponseDTO.setId(bus.getId());
        busResponseDTO.setNumberOfSeats(bus.getNumberOfSeats());
        busResponseDTO.setName(bus.getName());
        busResponseDTO.setLicensePlateNumber(bus.getLicensePlateNumber());
        busResponseDTO.setIsCarApproved(bus.getIsCarApproved());
        return busResponseDTO;
    }

    private CarDTO getCarDTO(Car car){
        CarDTO carDTO = new CarDTO();
        carDTO.setId(car.getId());
        carDTO.setName(car.getName());
        carDTO.setLicensePlateNumber(car.getLicensePlateNumber());
        carDTO.setIsCarApproved(car.getIsCarApproved() == null ? false : car.getIsCarApproved());
        carDTO.setIsOfficialAgencyIndicator(car.getIsOfficialAgencyIndicator() == null ? false : car.getIsOfficialAgencyIndicator());
        carDTO.setTimestamp(car.getTimestamp());
        return carDTO;
    }


}
