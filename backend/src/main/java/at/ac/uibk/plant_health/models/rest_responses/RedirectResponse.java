package at.ac.uibk.plant_health.models.rest_responses;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class RedirectResponse extends RestResponse {
	@JsonIgnore
	@Builder.Default
	private HttpStatusCode statusCode = HttpStatusCode.valueOf(HttpStatus.FOUND.value());

	@NonNull
	@JsonIgnore
	private String redirectLocation;

	private RedirectResponse() {
		super(HttpStatus.FOUND);
		this.redirectLocation = "/";
	}

	public RedirectResponse(String redirectLocation) {
		super(HttpStatus.FOUND);
		this.redirectLocation = redirectLocation;
	}

	@Override
	public RestResponseEntity toEntity() {
		if (this.redirectLocation != null) {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Location", redirectLocation);
			return new RestResponseEntity(this, headers);
		} else {
			return super.toEntity();
		}
	}

	// region Builder Customization
	public abstract static class RedirectResponseBuilder<
			C extends RedirectResponse, B extends RedirectResponseBuilder<C, B>>
			extends RestResponseBuilder<C, B> {
		@Override
		public B statusCode(int statusCode) {
			return this.statusCode(HttpStatusCode.valueOf(statusCode));
		}

		@Override
		public B statusCode(HttpStatus httpStatus) {
			return this.statusCode(HttpStatusCode.valueOf(httpStatus.value()));
		}

		@Override
		public B statusCode(HttpStatusCode httpStatusCode) {
			if (httpStatusCode.is3xxRedirection()) {
				return super.statusCode(httpStatusCode);
			} else {
				return super.statusCode(HttpStatus.FOUND);
			}
		}
	}
	// endregion
}
