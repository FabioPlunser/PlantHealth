package at.ac.uibk.plant_health.models.annotations;

import at.ac.uibk.plant_health.models.device.SensorStation;

@PrincipalRequired(SensorStation.class)
public @interface RequiresSensorStation {}
