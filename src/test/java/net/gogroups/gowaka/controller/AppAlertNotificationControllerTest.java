package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.dto.AppNoticeDTO;
import net.gogroups.gowaka.service.AppNoticeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

/**
 * Author: Edward Tanko <br/>
 * Date: 1/24/21 6:18 AM <br/>
 */
@ExtendWith(MockitoExtension.class)
class AppAlertNotificationControllerTest {

    @Mock
    private AppNoticeService appNoticeService;

    @InjectMocks
    private AppAlertNotificationController appAlertNotificationController;

    @Test
    void getAppNotices_return_AppNotice() {
        when(appNoticeService.getAllAppNotice())
                .thenReturn(Collections.singletonList(new AppNoticeDTO()));
        ResponseEntity<List<AppNoticeDTO>> response = appAlertNotificationController.getAppNotices();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    @Test
    void evictNotificationCache_return_string() {

        ResponseEntity<String> response = appAlertNotificationController.evictNotificationCache();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Cache evicted");
    }
}
