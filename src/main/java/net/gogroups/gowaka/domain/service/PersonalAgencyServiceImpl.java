package net.gogroups.gowaka.domain.service;

import net.gogroups.gowaka.domain.repository.PersonalAgencyRepository;
import net.gogroups.gowaka.domain.repository.UserRepository;
import net.gogroups.gowaka.domain.model.PersonalAgency;
import net.gogroups.gowaka.domain.model.User;
import net.gogroups.gowaka.dto.CreatePersonalAgencyDTO;
import net.gogroups.gowaka.dto.PersonalAgencyDTO;
import net.gogroups.gowaka.dto.UserDTO;
import net.gogroups.gowaka.exception.ResourceNotFoundException;
import net.gogroups.gowaka.service.PersonalAgencyService;
import net.gogroups.gowaka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/21/19 11:03 AM <br/>
 */
@Service
public class PersonalAgencyServiceImpl implements PersonalAgencyService {

    private UserService userService;
    private UserRepository userRepository;
    private PersonalAgencyRepository personalAgencyRepository;

    @Autowired
    public PersonalAgencyServiceImpl(UserService userService, UserRepository userRepository, PersonalAgencyRepository personalAgencyRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.personalAgencyRepository = personalAgencyRepository;
    }

    @Override
    public PersonalAgencyDTO createPersonalAgency(CreatePersonalAgencyDTO createPersonalAgencyDTO) {

        UserDTO currentAuthUser = userService.getCurrentAuthUser();

        Optional<User> userOptional = userRepository.findById(currentAuthUser.getId());
        if(!userOptional.isPresent()){
            throw new ResourceNotFoundException("User not found.");
        }
        //can only be one Personal Agency per user
        User user = userOptional.get();
        PersonalAgency personalAgency = user.getPersonalAgency();
        if(personalAgency==null) {
            personalAgency = new PersonalAgency();
        }
        personalAgency.setName(createPersonalAgencyDTO.getName());
        personalAgency.setUser(user);

        PersonalAgency savedPersonalAgency = personalAgencyRepository.save(personalAgency);

        user.setPersonalAgency(savedPersonalAgency);
        userRepository.save(user);

        PersonalAgencyDTO personalAgencyDTO = new PersonalAgencyDTO();
        personalAgencyDTO.setId(savedPersonalAgency.getId());
        personalAgencyDTO.setName(savedPersonalAgency.getName());

        return personalAgencyDTO;
    }
}
