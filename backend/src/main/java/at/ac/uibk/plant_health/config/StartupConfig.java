package at.ac.uibk.plant_health.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.UUID;

import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.service.PersonService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class StartupConfig {
	@Autowired
	private PersonService personService;

	/**
	 * Injected Name of the Active Profile specified in the Application
	 * Properties.
	 */
	@Value("${spring.profiles.active:}")
	private String activeProfileString;

	/**
	 * Gets the currently active "application.properties" Profile.
	 *
	 * @return The currently active Configuration Profile.
	 */
	@Bean
	public Profile getActiveProfile() {
		return Profile.fromString(activeProfileString);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void logActiveProfile() {
		Profile activeProfile = getActiveProfile();
		if (activeProfile.isUnknown()) {
			log.warn(String.format("Unknown Active Profile: \"%s\"", activeProfileString));
		} else {
			log.debug(String.format("Active Profile: \"%s\"", activeProfile));
		}
	}

	@EventListener(ApplicationReadyEvent.class)
	public void createBaseAdminUser() {
		Profile activeProfile = getActiveProfile();
		switch (activeProfile) {
						case DEBUG -> {
                String unhashedPassword = "password";
                Person person = new Person(
                        "Admin", "admin@noreply.com", unhashedPassword,
                        UUID.fromString("62b3e09e-c529-40c6-85c6-1afc53e17408"),
                        Permission.adminAuthorities()
                );
                if (this.personService.create(person)) {
                    log.info(String.format("Created User \"%s\" with Password \"%s\" and Token \"%s\"",
                            person.getUsername(), unhashedPassword, person.getToken()
                    ));
                }
            }
            default -> { /* Do nothing by default */ }
        }
    }

    /**
     * Helper Class for easier Handling of the possible Profiles.
     */
    public enum Profile {
        DEBUG,
        PROD,
        TEST,
        OTHER;

        public static Profile fromString(String string) {
            try {
                return Profile.valueOf(string.toUpperCase());
            } catch (Exception e) {
                return Profile.OTHER;
            }
        }

        public boolean isUnknown() {
            return this == OTHER;
        }
    }
}
