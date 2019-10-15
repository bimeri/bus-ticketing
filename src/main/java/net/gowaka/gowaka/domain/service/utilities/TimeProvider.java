package net.gowaka.gowaka.domain.service.utilities;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * @author Nnouka Stephen
 * @date 15 Oct 2019
 */
public class TimeProvider {
    protected static Clock clock = Clock.systemDefaultZone();
    public static LocalDateTime now(){
        return LocalDateTime.now(clock);
    }
}
