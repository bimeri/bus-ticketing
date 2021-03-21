package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.dto.AllAvailableJourneyAndBookedSeatsDTO;
import net.gogroups.gowaka.service.CacheDataProcessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/20/21 5:32 PM <br/>
 */
@ExtendWith(MockitoExtension.class)
class CacheDataProcessorControllerTest {

    @Mock
    private CacheDataProcessorService mockCacheDataProcessorService;

    private CacheDataProcessorController cacheDataProcessorController;

    @BeforeEach
    void setUp() {
        cacheDataProcessorController = new CacheDataProcessorController(mockCacheDataProcessorService);
    }

    @Test
    void getAllAvailableJourneys_calls_CacheDataProcessorService() {

        when(mockCacheDataProcessorService.getAllAvailableJourneys())
                .thenReturn(new AllAvailableJourneyAndBookedSeatsDTO());
        cacheDataProcessorController.getAllAvailableJourneys();
        verify(mockCacheDataProcessorService).getAllAvailableJourneys();
    }

}
