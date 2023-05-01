package at.ac.uibk.plant_health.config.jwt_authentication;

import at.ac.uibk.plant_health.config.jwt_authentication.authentication_types.AccessPointAuthentication;
import at.ac.uibk.plant_health.config.jwt_authentication.authentication_types.SensorStationAuthentication;
import at.ac.uibk.plant_health.config.jwt_authentication.authentication_types.TokenAuthentication;
import at.ac.uibk.plant_health.config.jwt_authentication.authentication_types.UserAuthentication;
import at.ac.uibk.plant_health.util.ConversionUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthenticationFactory {
	public static TokenAuthentication getAuthentication(
			String userAgent, String authorizationHeader
	) {
		return switch (userAgent) {
			case "SensorStation" -> ConversionUtil.tryConvertJson(authorizationHeader, SensorStationAuthentication.class);
            case "AccessPoint" -> ConversionUtil.tryConvertJson(authorizationHeader, AccessPointAuthentication.class);
            default -> ConversionUtil.tryConvertJson(authorizationHeader, UserAuthentication.class);
        };
    }
}
