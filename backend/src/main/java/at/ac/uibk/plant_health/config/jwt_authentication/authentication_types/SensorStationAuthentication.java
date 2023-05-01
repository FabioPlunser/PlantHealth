package at.ac.uibk.plant_health.config.jwt_authentication.authentication_types;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SensorStationAuthentication extends TokenAuthentication {}
