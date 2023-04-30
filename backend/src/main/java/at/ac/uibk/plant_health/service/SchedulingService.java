package at.ac.uibk.plant_health.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

@Profile("!test")
@Configuration
@EnableScheduling
@Slf4j
public class SchedulingService {
    @Autowired
    private AccessPointService accessPointService;

    @Scheduled(fixedDelayString = "${swa.scheduling.rate.in.seconds}", timeUnit = TimeUnit.SECONDS)
    public void checkAccessPointConnections() {
        log.info("Scheduled Check if AccessPoints are still connected: %d changed\n", accessPointService.updateLastConnection());
    }
}
