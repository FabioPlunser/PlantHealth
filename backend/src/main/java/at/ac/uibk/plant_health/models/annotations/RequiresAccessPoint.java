package at.ac.uibk.plant_health.models.annotations;

import at.ac.uibk.plant_health.models.device.AccessPoint;

@PrincipalRequired(AccessPoint.class)
public @interface RequiresAccessPoint {}
