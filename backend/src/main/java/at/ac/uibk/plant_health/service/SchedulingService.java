package at.ac.uibk.plant_health.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
public class SchedulingService {
    @Autowired
    private AccessPointService accessPointService;

    @Scheduled(fixedDelayString = "${swa.scheduling.rate.in.seconds}", timeUnit = TimeUnit.SECONDS)
    public void checkAccessPointConnections() {
        System.out.printf("Scheduled Check if AccessPoints are still connected: %d changed\n", accessPointService.updateLastConnection());
    }
}
