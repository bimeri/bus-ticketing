package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.dto.StaticContentDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Author: Edward Tanko <br/>
 * Date: 2/18/21 7:23 PM <br/>
 */
class StaticContentControllerTest {

    private StaticContentController staticContentController;

    @BeforeEach
    void setUp() {
        staticContentController = new StaticContentController();
    }

    @Test
    void getAboutUs() {
        ResponseEntity<StaticContentDTO> aboutUs = staticContentController.getAboutUs();
        assertThat(aboutUs.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getTermsAndCondition() {
        ResponseEntity<StaticContentDTO> termsAndCondition = staticContentController.getTermsAndCondition();
        assertThat(termsAndCondition.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
