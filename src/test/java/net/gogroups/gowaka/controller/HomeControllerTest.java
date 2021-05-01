package net.gogroups.gowaka.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Author: Edward Tanko <br/>
 * Date: 1/24/21 5:29 AM <br/>
 */
@ExtendWith(MockitoExtension.class)
class HomeControllerTest {


    private HomeController homeController = new HomeController();

    @Test
    void showHomePage_return_home_page() {
        String homePage = homeController.showHomePage();
        assertThat(homePage).isEqualTo("forward:/index.html");
    }

}
