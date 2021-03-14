package net.gogroups.gowaka.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.gogroups.gowaka.constant.UserRoles;
import net.gogroups.gowaka.domain.config.ClientUserCredConfig;
import net.gogroups.gowaka.domain.model.*;
import net.gogroups.gowaka.domain.repository.AgencyBranchRepository;
import net.gogroups.gowaka.domain.repository.JourneyRepository;
import net.gogroups.gowaka.domain.repository.OfficialAgencyRepository;
import net.gogroups.gowaka.domain.repository.UserRepository;
import net.gogroups.gowaka.dto.*;
import net.gogroups.gowaka.exception.ApiException;
import net.gogroups.gowaka.exception.ErrorCodes;
import net.gogroups.gowaka.exception.ResourceNotFoundException;
import net.gogroups.gowaka.service.OfficialAgencyService;
import net.gogroups.gowaka.service.UserService;
import net.gogroups.security.model.ApiSecurityAccessToken;
import net.gogroups.security.model.ApiSecurityClientUser;
import net.gogroups.security.model.ApiSecurityUser;
import net.gogroups.security.service.ApiSecurityService;
import net.gogroups.storage.constants.FileAccessType;
import net.gogroups.storage.service.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.gogroups.gowaka.constant.UserRoles.*;


/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 8:23 PM <br/>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OfficialAgencyServiceImpl implements OfficialAgencyService {

    private static final String LOGO_DIRECTORY = "agency_logos";
    private static final String MAIN_BRANCH = "Main Branch";

    private final OfficialAgencyRepository officialAgencyRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ApiSecurityService apiSecurityService;
    private final ClientUserCredConfig clientUserCredConfig;
    private final FileStorageService fileStorageService;
    private final JourneyRepository journeyRepository;
    private final AgencyBranchRepository agencyBranchRepository;


    @Override
    public OfficialAgencyDTO createOfficialAgency(CreateOfficialAgencyDTO createOfficialAgencyDTO) {

        ApiSecurityAccessToken clientToken = getApiSecurityAccessToken();

        String username = createOfficialAgencyDTO.getAgencyAdminEmail();
        ApiSecurityUser apiSecurityUser = apiSecurityService.getUserByUsername(username, clientToken.getToken());


        String userRole = apiSecurityUser.getRoles() + AGENCY_ADMIN.toString();
        apiSecurityService.updateUserInfo(apiSecurityUser.getId(), "ROLES", userRole, clientToken.getToken());

        Optional<User> userOptional = userRepository.findById(apiSecurityUser.getId());
        if (!userOptional.isPresent()) {
            throw new ApiException("Agency admin user not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        User user = userOptional.get();

        if (user.getOfficialAgency() != null) {
            throw new ApiException("User already a member of an agency.", ErrorCodes.USER_ALREADY_IN_AN_AGENCY.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName(createOfficialAgencyDTO.getAgencyName());
        officialAgency.setAgencyRegistrationNumber(createOfficialAgencyDTO.getAgencyRegistrationNumber());
        List<User> agencyUsers = officialAgency.getUsers();
        agencyUsers.add(user);
        officialAgency.setIsDisabled(false);
        officialAgency.setPolicy(createOfficialAgencyDTO.getPolicy());
        officialAgency.setCode(createOfficialAgencyDTO.getCode());

        OfficialAgency saveOfficialAgency = officialAgencyRepository.save(officialAgency);

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setName(MAIN_BRANCH);
        AgencyBranch savedAgencyBranch = agencyBranchRepository.save(agencyBranch);

        user.setOfficialAgency(officialAgency);
        user.setAgencyBranch(savedAgencyBranch);
        user.setIsAgencyAdminIndicator(true);
        userRepository.save(user);


        OfficialAgencyDTO officialAgencyDTO = new OfficialAgencyDTO();
        OfficialAgencyAdminUserDTO agencyAdminDTO = new OfficialAgencyAdminUserDTO();
        agencyAdminDTO.setId(apiSecurityUser.getId());
        agencyAdminDTO.setFullName(apiSecurityUser.getFullName());
        officialAgencyDTO.setId(saveOfficialAgency.getId());
        officialAgencyDTO.setAgencyName(saveOfficialAgency.getAgencyName());
        officialAgencyDTO.setAgencyRegistrationNumber(saveOfficialAgency.getAgencyRegistrationNumber());
        officialAgencyDTO.setAgencyAdmin(agencyAdminDTO);
        officialAgencyDTO.setPolicy(saveOfficialAgency.getPolicy());
        officialAgencyDTO.setCode(saveOfficialAgency.getCode());

        OfficialAgencyDTO.Branch branch = new OfficialAgencyDTO.Branch();
        branch.setId(savedAgencyBranch.getId());
        branch.setName(savedAgencyBranch.getName());
        branch.setAddress(savedAgencyBranch.getAddress());
        branch.setUpdatedAt(savedAgencyBranch.getUpdatedAt());
        branch.setUpdatedBy(savedAgencyBranch.getUpdatedBy());
        officialAgencyDTO.setBranches(Collections.singletonList(branch));
        officialAgencyDTO.setUpdatedAt(savedAgencyBranch.getUpdatedAt());
        officialAgencyDTO.setUpdatedBy(savedAgencyBranch.getUpdatedBy());

        return officialAgencyDTO;

    }

    @Override
    public void uploadAgencyLogo(Long agencyId, MultipartFile logoFile) {

        Optional<OfficialAgency> officialAgencyOptional = officialAgencyRepository.findById(agencyId);
        if (!officialAgencyOptional.isPresent()) {
            throw new ResourceNotFoundException("Agency not found.");
        }
        OfficialAgency officialAgency = officialAgencyOptional.get();

        String filename = StringUtils.cleanPath(logoFile.getOriginalFilename());

        byte[] fileByteArray = null;
        try {
            fileByteArray = logoFile.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String storageFolder = LOGO_DIRECTORY + "/" + agencyId;
        fileStorageService.saveFile(filename, fileByteArray, storageFolder, FileAccessType.PROTECTED);

        officialAgency.setLogo(storageFolder + "/" + filename);
        officialAgencyRepository.save(officialAgency);
    }

    @Override
    public void updateOfficialAgency(Long agencyId, OfficialAgencyDTO officialAgencyDTO) {


        Optional<OfficialAgency> officialAgencyOptional = officialAgencyRepository.findById(agencyId);
        if (!officialAgencyOptional.isPresent()) {
            throw new ResourceNotFoundException("Agency not found.");
        }
        OfficialAgency officialAgency = officialAgencyOptional.get();
        officialAgency.setAgencyName(officialAgencyDTO.getAgencyName());
        officialAgency.setPolicy(officialAgencyDTO.getPolicy());
        officialAgency.setCode(officialAgencyDTO.getCode());
        officialAgency.setAgencyRegistrationNumber(officialAgencyDTO.getAgencyRegistrationNumber());

        officialAgencyRepository.save(officialAgency);
    }

    @Override
    public List<OfficialAgencyDTO> getAllAgencies() {

        return officialAgencyRepository.findAll().stream()
                .map(this::getOfficialAgencyDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OfficialAgencyDTO getUserAgency() {
        User user = getCurrentAuthUser();
        return getOfficialAgencyDTO(user.getOfficialAgency());
    }

    @Override
    public OfficialAgencyUserDTO assignAgencyUserRole(OfficialAgencyUserRoleRequestDTO officialAgencyUserRoleRequestDTO) {

        String userId = officialAgencyUserRoleRequestDTO.getUserId();

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new ApiException("User not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        User user = userOptional.get();

        UserDTO currentAuthUser = userService.getCurrentAuthUser();
        Optional<User> currentAuthUserOptional = userRepository.findById(currentAuthUser.getId());
        if (!currentAuthUserOptional.isPresent()) {
            throw new ApiException("User not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        User authUser = currentAuthUserOptional.get();

        if (!user.getOfficialAgency().getId().equals(authUser.getOfficialAgency().getId())) {
            throw new ApiException("User must be a member to your agency.", ErrorCodes.USER_NOT_IN_AGENCY.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        List<UserRoles> systemRoles = Arrays.asList(UserRoles.values());

        List<String> roles = officialAgencyUserRoleRequestDTO.getRoles().stream()
                .filter(role -> systemRoles.contains(UserRoles.valueOf(role)))
                .filter(role -> !role.equalsIgnoreCase(AGENCY_ADMIN.toString()))
                .filter(role -> !role.equalsIgnoreCase(GW_ADMIN.toString()))
                .collect(Collectors.toList());
        StringBuilder userRole = new StringBuilder(USERS.toString());
        for (String role : roles) {
            userRole.append(";").append(role);
        }

        ApiSecurityAccessToken clientToken = getApiSecurityAccessToken();
        ApiSecurityUser apiSecurityUser = apiSecurityService.getUserByUserId(userId, clientToken.getToken());

        apiSecurityService.updateUserInfo(apiSecurityUser.getId(), "ROLES", userRole.toString(), clientToken.getToken());
        OfficialAgencyUserDTO officialAgencyUserDTO = new OfficialAgencyUserDTO();
        officialAgencyUserDTO.setFullName(apiSecurityUser.getFullName());
        officialAgencyUserDTO.setId(apiSecurityUser.getId());
        officialAgencyUserDTO.setRoles(Arrays.asList(userRole.toString().split(";")));

        return officialAgencyUserDTO;
    }

    @Override
    public List<OfficialAgencyUserDTO> getAgencyUsers() {

        ApiSecurityAccessToken clientToken = getApiSecurityAccessToken();
        UserDTO currentAuthUser = userService.getCurrentAuthUser();

        Optional<User> userOptional = userRepository.findById(currentAuthUser.getId());
        if (!userOptional.isPresent()) {
            throw new ApiException("User not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        User authUser = userOptional.get();
        OfficialAgency officialAgency = authUser.getOfficialAgency();
        if (officialAgency == null) {
            throw new ApiException("User Agency not found.", ErrorCodes.USER_NOT_IN_AGENCY.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        List<User> agencyUsers = officialAgency.getUsers();

        return agencyUsers.stream()
                .filter(user -> !user.getUserId().equals(authUser.getUserId()))
                .map(user -> {
                    try {
                        ApiSecurityUser apiSecurityUser = apiSecurityService.getUserByUserId(user.getUserId(), clientToken.getToken());
                        OfficialAgencyUserDTO officialAgencyUserDTO = new OfficialAgencyUserDTO();
                        officialAgencyUserDTO.setId(apiSecurityUser.getId());
                        officialAgencyUserDTO.setFullName(apiSecurityUser.getFullName());
                        officialAgencyUserDTO.setAgencyName(user.getOfficialAgency().getAgencyName());
                        officialAgencyUserDTO.setBranchName(user.getAgencyBranch().getName());
                        officialAgencyUserDTO.setBranchAddress(user.getAgencyBranch().getAddress());
                        officialAgencyUserDTO.setRoles(Arrays.asList(apiSecurityUser.getRoles().split(";")));
                        return officialAgencyUserDTO;
                    } catch (Exception ex) {
                        log.info("User <{}> data not in sync with ApiSecurity: {}", user.getUserId(), ex.getMessage());
                    }
                    return new OfficialAgencyUserDTO();
                })
                .filter(officialAgencyUserDTO -> officialAgencyUserDTO.getId() != null)
                .collect(Collectors.toList());
    }

    @Override
    public OfficialAgencyUserDTO addAgencyUser(EmailDTO emailDTO, Long branchId) {

        UserDTO authUserDTO = userService.getCurrentAuthUser();
        Optional<User> authUserOptional = userRepository.findById(authUserDTO.getId());
        if (!authUserOptional.isPresent()) {
            throw new ApiException("User not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        ApiSecurityAccessToken apiSecurityAccessToken = getApiSecurityAccessToken();
        ApiSecurityUser apiSecurityUser = apiSecurityService.getUserByUsername(emailDTO.getEmail(), apiSecurityAccessToken.getToken());

        Optional<User> userOptional = userRepository.findById(apiSecurityUser.getId());
        if (!userOptional.isPresent()) {
            throw new ApiException("User not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        User authUser = authUserOptional.get();
        User user = userOptional.get();
        if (user.getOfficialAgency() != null) {
            throw new ApiException("User already a member of an agency.", ErrorCodes.USER_ALREADY_IN_AN_AGENCY.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        OfficialAgency officialAgency = authUser.getOfficialAgency();
        Optional<AgencyBranch> agencyBranchOptional = officialAgency.getAgencyBranch().stream()
                .filter(agencyBranch -> agencyBranch.getId().equals(branchId))
                .findFirst();
        if (!agencyBranchOptional.isPresent()) {
            throw new ApiException("Not a valid agency branch", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        AgencyBranch agencyBranch = agencyBranchOptional.get();

        user.setOfficialAgency(officialAgency);
        user.setAgencyBranch(agencyBranch);
        userRepository.save(user);

        OfficialAgencyUserDTO officialAgencyUserDTO = new OfficialAgencyUserDTO();
        officialAgencyUserDTO.setFullName(apiSecurityUser.getFullName());
        officialAgencyUserDTO.setId(apiSecurityUser.getId());
        officialAgencyUserDTO.setAgencyName(officialAgency.getAgencyName());
        officialAgencyUserDTO.setBranchName(agencyBranch.getName());
        officialAgencyUserDTO.setBranchAddress(agencyBranch.getAddress());
        officialAgencyUserDTO.setRoles(Arrays.asList(apiSecurityUser.getRoles().split(";")));

        return officialAgencyUserDTO;
    }

    @Override
    public void removeAgencyUser(String userId) {

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new ApiException("User not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        User user = userOptional.get();

        UserDTO currentAuthUser = userService.getCurrentAuthUser();
        Optional<User> currentAuthUserOptional = userRepository.findById(currentAuthUser.getId());
        if (!currentAuthUserOptional.isPresent()) {
            throw new ApiException("User not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        User authUser = currentAuthUserOptional.get();
        if (!user.getOfficialAgency().getId().equals(authUser.getOfficialAgency().getId())) {
            throw new ApiException("User must be a member to your agency.", ErrorCodes.USER_NOT_IN_AGENCY.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (user.getUserId().equals(authUser.getUserId())) {
            throw new ApiException("Operation not allowed.", ErrorCodes.VALIDATION_ERROR.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        ApiSecurityAccessToken clientToken = getApiSecurityAccessToken();
        apiSecurityService.updateUserInfo(userId, "ROLES", USERS.toString(), clientToken.getToken());

        user.setOfficialAgency(null);
        user.setAgencyBranch(null);
        userRepository.save(user);

    }

    @Override
    public void createBranch(CreateBranchDTO createBranchDTO) {

        User currentAuthUser = getCurrentAuthUser();
        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setName(createBranchDTO.getName());
        agencyBranch.setAddress(createBranchDTO.getAddress());
        agencyBranch.setOfficialAgency(currentAuthUser.getOfficialAgency());
        agencyBranchRepository.save(agencyBranch);
    }

    @Override
    public void updateBranch(CreateBranchDTO createBranchDTO, Long branchId) {

        AgencyBranch agencyBranch = getAgencyBranch(branchId);
        User currentAuthUser = getCurrentAuthUser();
        verifyIfUserInAgency(agencyBranch, currentAuthUser);

        agencyBranch.setName(createBranchDTO.getName());
        agencyBranch.setAddress(createBranchDTO.getAddress());
        agencyBranchRepository.save(agencyBranch);
    }

    @Override
    public void deleteBranch(Long branchId) {

        AgencyBranch agencyBranch = getAgencyBranch(branchId);
        User currentAuthUser = getCurrentAuthUser();
        verifyIfUserInAgency(agencyBranch, currentAuthUser);

        agencyBranchRepository.deleteById(branchId);
    }

    private void verifyIfUserInAgency(AgencyBranch agencyBranch, User currentAuthUser) {
        if (!agencyBranch.getOfficialAgency().getId().equals(currentAuthUser.getOfficialAgency().getId())) {
            throw new ApiException("User not in agency", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
    }

    private AgencyBranch getAgencyBranch(Long branchId) {
        Optional<AgencyBranch> agencyBranchOptional = agencyBranchRepository.findById(branchId);
        if (!agencyBranchOptional.isPresent()) {
            throw new ApiException("Branch not found", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return agencyBranchOptional.get();
    }

    private ApiSecurityAccessToken getApiSecurityAccessToken() {
        ApiSecurityClientUser apiSecurityClientUser = new ApiSecurityClientUser();
        apiSecurityClientUser.setClientId(clientUserCredConfig.getClientId());
        apiSecurityClientUser.setClientSecret(clientUserCredConfig.getClientSecret());
        return apiSecurityService.getClientToken(apiSecurityClientUser);
    }

    private OfficialAgencyDTO getOfficialAgencyDTO(OfficialAgency agency) {

        long numberOfCompletedTrips = journeyRepository.findByArrivalIndicatorTrue().stream()
                .filter(journey -> {
                    Car car = journey.getCar();
                    if (car == null) return false;
                    if (car instanceof Bus) {
                        if (((Bus) car).getOfficialAgency() == null) return false;
                        return ((Bus) car).getOfficialAgency().getId().equals(agency.getId());
                    }
                    return false;
                }).count();

        String logoURL = fileStorageService.getFilePath(agency.getLogo(), "", FileAccessType.PROTECTED);
        List<OfficialAgencyDTO.Bus> buses = agency.getBuses().stream()
                .map(bus -> OfficialAgencyDTO.Bus.builder()
                        .id(bus.getId())
                        .name(bus.getName())
                        .licensePlateNumber(bus.getLicensePlateNumber())
                        .numberOfSeats(bus.getNumberOfSeats())
                        .updatedBy(bus.getUpdatedBy())
                        .updatedAt(bus.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
        List<OfficialAgencyDTO.Branch> branches = agency.getAgencyBranch().stream()
                .map(agencyBranch -> OfficialAgencyDTO.Branch.builder()
                        .id(agencyBranch.getId())
                        .name(agencyBranch.getName())
                        .address(agencyBranch.getAddress())
                        .updatedBy(agencyBranch.getUpdatedBy())
                        .updatedAt(agencyBranch.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        Optional<User> userOptional = agency.getUsers().stream().filter(User::getIsAgencyAdminIndicator).findFirst();
        OfficialAgencyAdminUserDTO officialAgencyAdminUserDTO = new OfficialAgencyAdminUserDTO();
        if (userOptional.isPresent()) {
            officialAgencyAdminUserDTO.setId(userOptional.get().getUserId());
            officialAgencyAdminUserDTO.setFullName(userOptional.get().getFullName());
            officialAgencyAdminUserDTO.setEmail(userOptional.get().getEmail());
        }

        return OfficialAgencyDTO.builder()
                .agencyRegistrationNumber(agency.getAgencyRegistrationNumber())
                .agencyName(agency.getAgencyName())
                .id(agency.getId())
                .logo(logoURL)
                .buses(buses)
                .branches(branches)
                .policy(agency.getPolicy())
                .code(agency.getCode())
                .agencyAdmin(officialAgencyAdminUserDTO)
                .numberOfCompletedTrips(numberOfCompletedTrips)
                .build();
    }

    private User getCurrentAuthUser() {
        UserDTO authUser = userService.getCurrentAuthUser();
        Optional<User> optionalUser = userRepository.findById(authUser.getId());
        if (!optionalUser.isPresent()) {
            throw new ApiException("User not found", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return optionalUser.get();
    }

}
