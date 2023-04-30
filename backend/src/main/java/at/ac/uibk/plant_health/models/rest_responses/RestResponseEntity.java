package at.ac.uibk.plant_health.models.rest_responses;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class RestResponseEntity extends ResponseEntity<RestResponse> {
	private static final int DEFAULT_STATUS_CODE = 404;

	public RestResponseEntity(@NonNull RestResponse body) {
		super(body, body.getStatusCode());
	}

	public RestResponseEntity(
			@Nullable RestResponse body, @Nullable MultiValueMap<String, String> headers
	) {
		super(body, headers,
			  body != null ? body.getStatusCode() : HttpStatusCode.valueOf(DEFAULT_STATUS_CODE));
	}
}
