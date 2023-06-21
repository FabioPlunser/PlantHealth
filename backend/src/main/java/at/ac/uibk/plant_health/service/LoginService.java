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
import at.ac.uibk.plant_health.models.Log;
import at.ac.uibk.plant_health.models.annotations.AuditLogAnnotation;
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

	public Optional<UserDetails> login(TokenAuthentication token) {
		Optional<UserDetails> retVal;
		if (token instanceof UserAuthentication userAuthentication) {
			retVal = personService
							 .findByUsernameAndToken(
									 userAuthentication.getUsername(), userAuthentication.getToken()
							 )
							 .map(p -> p);
		} else if (token instanceof AccessPointAuthentication accessPointAuthentication) {
			retVal = accessPointRepository.findByAccessToken(accessPointAuthentication.getToken())
							 .map(a -> a);
		} else if (token instanceof SensorStationAuthentication sensorStationAuthentication) {
			retVal = sensorStationRepository.findById(sensorStationAuthentication.getToken())
							 .map(s -> s);
		} else {
			throw new InsufficientAuthenticationException("Internal Error!");
		}
		return retVal;
	}
}
