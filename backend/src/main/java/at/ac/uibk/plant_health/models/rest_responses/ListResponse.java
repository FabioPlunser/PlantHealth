package at.ac.uibk.plant_health.models.rest_responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A Response Container for sending a List of items fetched on the server side.
 * Only if the success-Field is set to true then a List is sent.
 */
@Getter
@SuperBuilder
public class ListResponse<T extends Serializable> extends RestResponse implements Serializable {
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private final List<T> items;

	/**
	 * Indicate a successful fetch of items.
	 * If items is null, an empty list will be sent to prevent problems on
	 * the Front-End.
	 *
	 * @param items The Items to send with the request.
	 */
	public ListResponse(List<T> items) {
		// Ensure that a List is always sent.
		this.items = Optional.ofNullable(items).orElse(new ArrayList<>());
	}
}
