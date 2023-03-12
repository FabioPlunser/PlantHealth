package at.ac.uibk.plant_health.util;

import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Helper Class storing all scheduled Jobs.
 * <br>
 * See <a
 * href="https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#scheduling">Spring
 * Docs about Scheduling</a>
 */
// All your Constructors are belong to us!
@EnableScheduling
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SchedulerUtil {
		/*
		@Scheduled(fixedDelay = 1, timeUnit = TimeUnit.DAYS)
		public void doSomethingEveryDay() {
				return;
		}
		 */
}
