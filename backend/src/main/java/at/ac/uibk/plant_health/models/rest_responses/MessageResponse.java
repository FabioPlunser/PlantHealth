package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.MODULE)
@AllArgsConstructor
public class MessageResponse extends RestResponse implements Serializable {
	protected String message;

	public MessageResponse(int status, String message) {
		super(status);
		this.message = message;
	}

	// region Builder Customization
	public abstract static class MessageResponseBuilder<
			C extends MessageResponse, B extends MessageResponse.MessageResponseBuilder<C, B>>
			extends RestResponseBuilder<C, B> {
		@Override
		public B internal_error() {
			super.internal_error();
			return this.message("Internal Server Error!");
		}

		public B internal_error(Exception e) {
			super.internal_error();
			return this.message(e.getMessage());
		}
	}
	// endregion
}
