package at.ac.uibk.plant_health.controllers;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

import at.ac.uibk.plant_health.models.annotations.AnyPermission;
import at.ac.uibk.plant_health.models.rest_responses.*;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.service.SensorStationService;

public class PlantController {
	@Autowired
	private SensorStationService sensorStationService;

	@GetMapping("/get-all-plants")
	public RestResponseEntity getAllPlants() {
		return new PlantListResponse(sensorStationService.findAllPlants()).toEntity();
	}

	@GetMapping("/scan-qr-code")
	public RestResponseEntity scanQrCode(@RequestParam("qr-code") final UUID qrCode) {
		var plant = sensorStationService.findByQrCode(qrCode);
		if (plant.isPresent()) {
			return new QrCodeResponse(plant.get()).toEntity();
		}
		return MessageResponse.builder()
				.statusCode(HttpStatus.NOT_FOUND)
				.message("Could not find Plant")
				.toEntity();
	}

	@AnyPermission(Permission.ADMIN)
	@RequestMapping(
			value = "/create-plant-qr-code",
			method = {RequestMethod.PUT, RequestMethod.GET, RequestMethod.POST}
	)
	public RestResponseEntity
	createQrCode(
			// TODO
	) {
		throw new NotImplementedException();
	}
}
