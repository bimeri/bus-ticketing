package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.dto.UserDTO;
import net.gowaka.gowaka.service.CBSService;
import net.gowaka.gowaka.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Author: Edward Tanko <br/>
 * Date: 6/21/20 6:03 PM <br/>
 */
@RunWith(MockitoJUnitRunner.class)
public class CBSControllerTest {

    @Mock
    private CBSService mockCbsService;
    @Mock
    private UserService mockUserService;

    private CBSController cbsController;

    @Before
    public void setUp() {
        cbsController = new CBSController(mockCbsService, mockUserService);
    }

    @Test
    public void getAllBenefits_callCBSService() {
        cbsController.getAllBenefits();
        verify(mockCbsService).getAllAvailableBenefit();
    }

    @Test
    public void getAllUserBenefits() {

        UserDTO userDTO = new UserDTO();
        userDTO.setId("123456");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);
        cbsController.getAllUserBenefits();
        verify(mockUserService).getCurrentAuthUser();
        verify(mockCbsService).getAllUserAvailableBenefit("123456");
    }

    @Test
    public void getUserRewardPoints() {

        UserDTO userDTO = new UserDTO();
        userDTO.setId("123456");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);
        cbsController.getUserRewardPoints();
        verify(mockUserService).getCurrentAuthUser();
        verify(mockCbsService).getUserRewardPoints("123456");
    }
}
