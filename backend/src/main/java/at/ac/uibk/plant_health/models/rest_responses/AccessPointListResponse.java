package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import at.ac.uibk.plant_health.models.device.AccessPoint;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class AccessPointListResponse extends RestResponse {
	private final List<InnerAccessPoint> accessPoints;

	public AccessPointListResponse(List<AccessPoint> accessPoints) {
		this.accessPoints = accessPoints.stream().map(InnerAccessPoint::new).toList();
	}

	@Getter
	private static class InnerAccessPoint implements Serializable {
		private final UUID accessPointId;
		private final UUID selfAssignedId;
		private final String roomName;
		private final boolean unlocked;
		private final boolean scanActive;
		private final boolean connected;
		private final int transferInterval;
		private final AdminSensorStationsResponse sensorStations;

		public InnerAccessPoint(AccessPoint accessPoint) {
			this.accessPointId = accessPoint.getDeviceId();
			this.selfAssignedId = accessPoint.getSelfAssignedId();
			this.roomName = accessPoint.getRoomName();
			this.unlocked = accessPoint.isUnlocked();
			this.scanActive = accessPoint.getScanActive();
			this.connected = accessPoint.isConnected();
			this.transferInterval = accessPoint.getTransferInterval();
			this.sensorStations = new AdminSensorStationsResponse(accessPoint.getSensorStations());
		}
	}
}