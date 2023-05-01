package at.ac.uibk.plant_health.config.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import at.ac.uibk.plant_health.models.annotations.AnyPermission;
import at.ac.uibk.plant_health.models.annotations.PrincipalRequired;
import at.ac.uibk.plant_health.models.annotations.PublicEndpoint;
import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.rest_responses.MessageResponse;
import at.ac.uibk.plant_health.models.rest_responses.RestResponseEntity;
import at.ac.uibk.plant_health.models.user.Authenticable;
import at.ac.uibk.plant_health.models.user.Permission;

@RestController
public class TestController {
	public final static String TEST_PERMISSION_ANONYMOUS = "/test/principle/anonymous";
	public final static String TEST_PERMISSION_API = "/test/principle/api";
	public final static String TEST_PERMISSION_ADMIN = "/test/principle/admin";

	public final static String TEST_PRINCIPLE_AUTHENTICABLE = "/test/principle/authenticable";
	public final static String TEST_PRINCIPLE_ACCESS_POINT = "/test/principle/accesspoint";
	public final static String TEST_PRINCIPLE_SENSOR_STATION = "/test/principle/sensorstation";

	@GetMapping(TEST_PERMISSION_ANONYMOUS)
	@PublicEndpoint
	public RestResponseEntity testAPI() {
		return MessageResponse.builder().ok().message("").toEntity();
	}

	@GetMapping(TEST_PERMISSION_API)
	@AnyPermission(Permission.USER)
	public RestResponseEntity testAPIAdmin() {
		return MessageResponse.builder().ok().message("").toEntity();
	}

	@GetMapping(TEST_PERMISSION_ADMIN)
	@AnyPermission(Permission.ADMIN)
	public RestResponseEntity testAdmin() {
		return MessageResponse.builder().ok().message("").toEntity();
	}

	@GetMapping(TEST_PRINCIPLE_AUTHENTICABLE)
	@PrincipalRequired(Authenticable.class)
	public RestResponseEntity testAuthenticable() {
		return MessageResponse.builder().ok().message("").toEntity();
	}

	@GetMapping(TEST_PRINCIPLE_ACCESS_POINT)
	@PrincipalRequired(AccessPoint.class)
	public RestResponseEntity testAccessPoint() {
		return MessageResponse.builder().ok().message("").toEntity();
	}

	@GetMapping(TEST_PRINCIPLE_SENSOR_STATION)
	@PrincipalRequired(SensorStation.class)
	public RestResponseEntity testSensorStation() {
		return MessageResponse.builder().ok().message("").toEntity();
	}
}
