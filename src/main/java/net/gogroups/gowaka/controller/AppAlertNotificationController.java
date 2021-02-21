package net.gogroups.gowaka.controller;

import lombok.extern.slf4j.Slf4j;
import net.gogroups.gowaka.dto.AppNoticeDTO;
import net.gogroups.gowaka.service.AppNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 1/24/21 6:16 AM <br/>
 */
@RestController
@Slf4j
public class AppAlertNotificationController {


    private AppNoticeService appNoticeService;

    @Autowired
    public AppAlertNotificationController(AppNoticeService appNoticeService) {
        this.appNoticeService = appNoticeService;
    }

    @GetMapping("api/public/notice")
    public ResponseEntity<List<AppNoticeDTO>> getAppNotices() {
        log.info("getting notification alerts");
        return ResponseEntity.ok(appNoticeService.getAllAppNotice());
    }

    @GetMapping("api/public/evict_cache")
    @CacheEvict(value = "alert_notice", allEntries = true)
    public ResponseEntity<String> evictNotificationCache() {
        log.info("evicting notification alerts from cache");
        return ResponseEntity.ok("Cache evicted");
    }
}
