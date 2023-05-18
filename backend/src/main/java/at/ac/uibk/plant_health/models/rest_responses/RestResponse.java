package at.ac.uibk.plant_health.models.rest_responses;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.io.Serializable;

import at.ac.uibk.plant_health.util.SerializationUtil;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Base Class for sending back JSON-Data from REST-Endpoints.
 * Contains a "success"-Field so the Front-End can determine
 * if an Operation was completed successfully.
 * Also contains a "statusCode"-Field which the {@link
 * org.springframework.http.ResponseEntity} can use to set custom Error Codes.
 *
 * @author davirieser
 * @version 1.1
 */
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.MODULE)
public abstract class RestResponse implements Serializable {
	// region Constructors
	protected RestResponse(int status) {
		this(HttpStatusCode.valueOf(status));
	}
	protected RestResponse(HttpStatus status) {
		this(status.value());
	}
	// endregion

	// region Status Code
	@JsonIgnore
	@Builder.Default
	private HttpStatusCode statusCode = HttpStatusCode.valueOf(HttpStatus.OK.value());
	// endregion

	// region Response Conversions
	@SuppressWarnings("unused")
	public String toResponse() {
		return SerializationUtil.serializeJSON(this);
	}

	public RestResponseEntity toEntity() {
		return new RestResponseEntity(this);
	}
	// endregion

	// region Builder Customization
	public abstract static class RestResponseBuilder<C extends RestResponse, B
															 extends RestResponseBuilder<C, B>> {
		/**
		 * Indicate that the Operation succeeded.
		 */
		public B ok() {
			return this.statusCode(200);
		}

		/**
		 * Indicate that an Internal Server Error occured.
		 */
		public B notFound() {
			return this.statusCode(404);
		}

		/**
		 * Indicate that an Internal Server Error occured.
		 */
		public B internalError() {
			return this.statusCode(501);
		}

		/**
		 * Manually set the Status Code of the Response.
		 *
		 * @param statusCode The Status Code to set
		 */
		public B statusCode(int statusCode) {
			return this.statusCode(HttpStatusCode.valueOf(statusCode));
		}

		/**
		 * Manually set the Status Code of the Response.
		 *
		 * @param httpStatus The Status Code to set
		 */
		public B statusCode(HttpStatus httpStatus) {
			return this.statusCode(httpStatus.value());
		}

		/**
		 * Manually set the Status Code of the Response.
		 *
		 * @param httpStatusCode The Status Code to set
		 */
		public B statusCode(HttpStatusCode httpStatusCode) {
			this.statusCode$value = httpStatusCode;
			this.statusCode$set = true;

			return (B) this;
		}

		/**
		 * Automatically the {@link RestResponse} into a {@link
		 * RestResponseEntity}
		 *
		 * @return The {@link RestResponseEntity} containing the built
		 *     {@link RestResponse}.
		 */
		public RestResponseEntity toEntity() {
			return new RestResponseEntity(this.build());
		}
	}
	// endregion
}
