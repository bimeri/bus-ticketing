package net.gogroups.gowaka;

import net.gogroups.gowaka.domain.service.utilities.TimeProvider;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author Nnouka Stephen
 * @date 15 Oct 2019
 */
public class TimeProviderTestUtil extends TimeProvider {
    private static ZoneId zoneId = ZoneId.of("GMT");
    public static void useFixedClockAt(LocalDateTime date) {
        clock = Clock.fixed(date.atZone(zoneId).toInstant(), zoneId);
    }
    public static void useSystemClock() {
        clock = Clock.systemDefaultZone();
    }
}
