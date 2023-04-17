package at.ac.uibk.plant_health.controllers;

import at.ac.uibk.plant_health.models.rest_responses.LogResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import at.ac.uibk.plant_health.models.Log;
import at.ac.uibk.plant_health.models.annotations.AnyPermission;
import at.ac.uibk.plant_health.models.rest_responses.ListResponse;
import at.ac.uibk.plant_health.models.rest_responses.RestResponseEntity;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.service.LogService;

@RestController
public class LogController {
	@Autowired
	private LogService logService;

	@AnyPermission(Permission.ADMIN)
	@GetMapping("/get-logs")
	public RestResponseEntity getLogs(
			@RequestParam("start") final LocalDateTime start,
			@RequestParam("end") final LocalDateTime end
	) {
		return new LogResponse(logService.findBetween(start, end)).toEntity();
	}


}
