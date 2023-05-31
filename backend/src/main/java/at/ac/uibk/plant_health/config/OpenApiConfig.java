package at.ac.uibk.plant_health.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
// NOTE: Explicitly enable Swagger using Application Settings
// (https://reflectoring.io/dont-use-spring-profile-annotation/)
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true")
public class OpenApiConfig {
	public static final String BEARER_KEY = "bearer-key";

	@Bean
	public OpenAPI customOpenAPI() {
		// https://swagger.io/docs/specification/authentication/
		Info info =
				new Info()
						.title("Tutorial Management API")
						.version("3.0")
						.description("This API exposes endpoints to be implemented in the frontend"
						);
		return new OpenAPI()
				.info(info)
				.addSecurityItem(new SecurityRequirement().addList(BEARER_KEY))
				.components(new Components().addSecuritySchemes(
						BEARER_KEY,
						new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
				));
	}
}
