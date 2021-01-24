package net.gogroups.gowaka.domain.service;

import net.gogroups.gowaka.domain.model.AppAlertNotice;
import net.gogroups.gowaka.domain.repository.AppAlertNoticeRepository;
import net.gogroups.gowaka.dto.AppNoticeDTO;
import net.gogroups.gowaka.service.AppNoticeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

/**
 * Author: Edward Tanko <br/>
 * Date: 1/24/21 6:00 AM <br/>
 */
@ExtendWith(MockitoExtension.class)
class AppNoticeServiceImplTest {

    @Mock
    private AppAlertNoticeRepository mockAppAlertNoticeRepository;

    private AppNoticeService appNoticeService;

    @BeforeEach
    void setUp() {
        appNoticeService =  new AppNoticeServiceImpl(mockAppAlertNoticeRepository);
    }

    @Test
    void getAllAppNotice_retrunAListOfNotifications() {
        when(mockAppAlertNoticeRepository.findByStatus(true))
                .thenReturn(Collections.singletonList(new AppAlertNotice(12L, "hello world", "en", true)));
        List<AppNoticeDTO> allAppNotice = appNoticeService.getAllAppNotice();
        assertThat(allAppNotice.get(0).getLanguage()).isEqualTo("en");
        assertThat(allAppNotice.get(0).getMessage()).isEqualTo("hello world");
    }
}
