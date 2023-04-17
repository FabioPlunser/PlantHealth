package at.ac.uibk.plant_health.controllers;

import at.ac.uibk.plant_health.models.rest_responses.LockedSensorStationResponse;
import at.ac.uibk.plant_health.models.rest_responses.MessageResponse;
import at.ac.uibk.plant_health.models.rest_responses.QrCodeResponse;
import at.ac.uibk.plant_health.models.rest_responses.RestResponseEntity;
import at.ac.uibk.plant_health.service.SensorStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

public class PlantController {
    @Autowired
    private SensorStationService sensorStationService;

    @GetMapping("/scan-qr-code")
    public RestResponseEntity getSensorStations(
            @RequestParam("qr-code") final UUID qrCode
    ) {
        var plant = sensorStationService.findByQrCode(qrCode);
        if (plant.isPresent()) {
            return new QrCodeResponse(plant.get()).toEntity();
        }
        return MessageResponse.builder()
                .statusCode(HttpStatus.NOT_FOUND)
                .message("Could not find Plant")
                .toEntity();
    }
}
