package at.ac.uibk.plant_health.models.rest_responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import at.ac.uibk.plant_health.models.device.AccessPoint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class AccessPointListResponse extends RestResponse {
	private final List<InnerAccessPoint> accessPoints;

	public AccessPointListResponse(List<AccessPoint> accessPoints) {
		this.accessPoints = accessPoints.stream().map(InnerAccessPoint::new).toList();
	}

	@Getter
	private class InnerAccessPoint implements Serializable {
		private final UUID accessPointId;
		private final String roomName;
		private final boolean unlocked;
		private final boolean scanActive;
		private final int transferInterval;

		public InnerAccessPoint(AccessPoint accessPoint) {
			this.accessPointId = accessPoint.getDeviceId();
			this.roomName = accessPoint.getRoomName();
			this.unlocked = accessPoint.isUnlocked();
			this.scanActive = accessPoint.getScanActive();
			this.transferInterval = accessPoint.getTransferInterval();
		}
	}
}