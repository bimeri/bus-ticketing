package net.gowaka.gowaka.domain.service;


import net.gowaka.gowaka.domain.model.*;
import net.gowaka.gowaka.domain.repository.CarRepository;
import net.gowaka.gowaka.domain.repository.PersonalAgencyRepository;
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
import java.util.ArrayList;
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
    private PersonalAgencyRepository personalAgencyRepository;

    @Autowired
    public CarServiceImpl(CarRepository carRepository, UserService userService, UserRepository userRepository) {
        this.carRepository = carRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Autowired
    public void setPersonalAgencyRepository(PersonalAgencyRepository personalAgencyRepository) {
        this.personalAgencyRepository = personalAgencyRepository;
    }

    @Override
    public ResponseBusDTO addOfficialAgencyBus(BusDTO busDTO) {
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
        return getResponseBusDTO(savedbus);
    }

    @Override
    public ResponseSharedRideDTO addSharedRide(SharedRideDTO sharedRideDTO) {
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
        verifyCurrentAuthUser();
        Car car = verifyCarById(id);
        car.setIsCarApproved(approveCarDTO.isApprove());
        carRepository.save(car);
    }

    @Override
    public List<ResponseSharedRideXDTO> getAllUnapprovedSharedRides() {
        verifyCurrentAuthUser();
        List<ResponseSharedRideXDTO> responseSharedRideXDTOS = new ArrayList<>();
        getPersonalAgencies().forEach(
                personalAgency -> {
                    List<SharedRide> sharedRides = personalAgency.getSharedRides();
                    if (!sharedRides.isEmpty()){
                        sharedRides.stream().filter(sharedRide -> sharedRide.getIsCarApproved() == null ||
                                !sharedRide.getIsCarApproved()).forEach(
                                        sharedRide -> responseSharedRideXDTOS.add(getResponseSharedRideXDTO(sharedRide))
                        );
                    }
                }
        );
        return responseSharedRideXDTOS;
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
        responseSharedRideDTO.setIsCarApproved(sharedRide.getIsCarApproved());
        return responseSharedRideDTO;
    }

    private ResponseBusDTO getResponseBusDTO(Bus bus){
        ResponseBusDTO responseBusDTO = new ResponseBusDTO();
        responseBusDTO.setId(bus.getId());
        responseBusDTO.setNumberOfSeats(bus.getNumberOfSeats());
        responseBusDTO.setName(bus.getName());
        responseBusDTO.setLicensePlateNumber(bus.getLicensePlateNumber());
        responseBusDTO.setIsCarApproved(bus.getIsCarApproved());
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

    private void verifyCarLicensePlateNumber(String licensePlateNumber){
        if (licensePlateNumber != null && carRepository.findByLicensePlateNumberIgnoreCase(licensePlateNumber.trim()).isPresent()){
            throw new ApiException("License plate number already in use",
                    ErrorCodes.LICENSE_PLATE_NUMBER_ALREADY_IN_USE.toString(), HttpStatus.CONFLICT);
        }
    }

    private Car verifyCarById(Long id){
        Optional<Car> optionalCar = carRepository.findById(id);
        if (!optionalCar.isPresent()){
            throw new ApiException("Car not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return optionalCar.get();
    }

    /**
     * specific to unapproved shared rides
     * @param sharedRide
     * @return
     */
    private ResponseSharedRideXDTO getResponseSharedRideXDTO(SharedRide sharedRide){
        ResponseSharedRideXDTO responseSharedRideXDTO = new ResponseSharedRideXDTO();
        responseSharedRideXDTO.setId(sharedRide.getId());
        responseSharedRideXDTO.setName(sharedRide.getName());
        responseSharedRideXDTO.setLicensePlateNumber(sharedRide.getLicensePlateNumber());
        responseSharedRideXDTO.setCarOwnerIdNumber(sharedRide.getCarOwnerIdNumber());
        responseSharedRideXDTO.setCarOwnerName(sharedRide.getCarOwnerName());
        responseSharedRideXDTO.setIsCarApproved(sharedRide.getIsCarApproved());
        responseSharedRideXDTO.setIsOfficialAgencyIndicator(sharedRide.getIsOfficialAgencyIndicator());
        responseSharedRideXDTO.setIsOfficialAgencyIndicator(sharedRide.getIsOfficialAgencyIndicator());
        responseSharedRideXDTO.setPersonalAgency(sharedRide.getPersonalAgency().getName());
        responseSharedRideXDTO.setTimestamp(sharedRide.getTimestamp());
        return responseSharedRideXDTO;
    }

    private List<PersonalAgency> getPersonalAgencies(){
        List<PersonalAgency> personalAgencies = personalAgencyRepository.findAll();
        if(personalAgencies.isEmpty()){
            throw new ApiException("No Personal Agency found.",
                    ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return personalAgencies;
    }

}
