package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
public class MessageResponse extends RestResponse implements Serializable {
	protected final String message;

	public MessageResponse(int status, String message) {
		super(status);
		this.message = message;
	}

	// region Builder Customization
	public abstract static class MessageResponseBuilder<
			C extends MessageResponse, B extends MessageResponse.MessageResponseBuilder<C, B>>
			extends RestResponseBuilder<C, B> {
		@Override
		public B internalError() {
			super.internalError();
			return this.message("Internal Server Error!");
		}

		public B internalError(Exception e) {
			super.internalError();
			return this.message(e.getMessage());
		}
	}
	// endregion
}
