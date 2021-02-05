package net.gogroups.gowaka.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.gogroups.gowaka.domain.repository.AppAlertNoticeRepository;
import net.gogroups.gowaka.dto.AppNoticeDTO;
import net.gogroups.gowaka.service.AppNoticeService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: Edward Tanko <br/>
 * Date: 1/24/21 5:36 AM <br/>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppNoticeServiceImpl implements AppNoticeService {

    private final AppAlertNoticeRepository appAlertNoticeRepository;

    @Override
    @Cacheable("alert_notice")
    public List<AppNoticeDTO> getAllAppNotice() {
        log.info("getting new alerts into cache...");
        return appAlertNoticeRepository.findByStatus(Boolean.TRUE).stream()
                .map(appAlertNotice -> new AppNoticeDTO(appAlertNotice.getTitle(), appAlertNotice.getMessage(), appAlertNotice.getLanguage()))
                .collect(Collectors.toList());
    }

}
