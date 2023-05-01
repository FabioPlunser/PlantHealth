package at.ac.uibk.plant_health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class SwaApplication {
	public static void main(String[] args) {
		SpringApplication.run(SwaApplication.class, args);
	}

	// region Password Encryption Bean
	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}
	// endregion
}