package at.ac.uibk.plant_health.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

import at.ac.uibk.plant_health.config.jwt_authentication.authentication_types.AccessPointAuthentication;
import at.ac.uibk.plant_health.config.jwt_authentication.authentication_types.SensorStationAuthentication;
import at.ac.uibk.plant_health.config.jwt_authentication.authentication_types.TokenAuthentication;
import at.ac.uibk.plant_health.config.jwt_authentication.authentication_types.UserAuthentication;
import at.ac.uibk.plant_health.repositories.AccessPointRepository;
import at.ac.uibk.plant_health.repositories.SensorStationRepository;

@Service
public class LoginService {
	@Autowired
	private PersonService personService;

	@Autowired
	private AccessPointRepository accessPointRepository;

	@Autowired
	private SensorStationRepository sensorStationRepository;

	public Optional<? extends UserDetails> login(TokenAuthentication token) {
		if (token instanceof UserAuthentication userAuthentication) {
			return personService.findByUsernameAndToken(
					userAuthentication.getUsername(), userAuthentication.getToken()
			);
		} else if (token instanceof AccessPointAuthentication accessPointAuthentication) {
			return accessPointRepository.findByAccessToken(accessPointAuthentication.getToken());
		} else if (token instanceof SensorStationAuthentication sensorStationAuthentication) {
			return sensorStationRepository.findById(sensorStationAuthentication.getToken());
		} else {
			throw new InsufficientAuthenticationException("Internal Error!");
		}
	}
}
