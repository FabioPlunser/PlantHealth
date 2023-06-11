package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorData;
import at.ac.uibk.plant_health.models.user.Person;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class SensorStationBaseResponse extends RestResponse implements Serializable {
	private final UUID sensorStationId;
	private final String bdAddress;
	private final int dipSwitchId;
	private final String roomName;
	private final String name;
	private final int transferInterval;
	private final Person gardener;
	private final List<AlarmResponse> alarms;
	private final boolean unlocked;
	private final boolean accessPointUnlocked;
	private final boolean connected;
	private final boolean deleted;

	public SensorStationBaseResponse(SensorStation sensorStation) {
		this.sensorStationId = sensorStation.getDeviceId();
		this.bdAddress = sensorStation.getBdAddress();
		this.name = sensorStation.getName();
		//-----------------------------------------------------------------------------------------
		var accessPoint = sensorStation.getAccessPoint();
		if (accessPoint != null) {
			this.roomName = accessPoint.getRoomName();
			this.transferInterval = accessPoint.getTransferInterval();
			this.accessPointUnlocked = accessPoint.isUnlocked();
		} else {
			this.roomName = null;
			this.transferInterval = 0;
			this.accessPointUnlocked = true;
		}
		//-----------------------------------------------------------------------------------------
		this.dipSwitchId = sensorStation.getDipSwitchId();
		this.alarms = sensorStation.getSensorData()
							  .stream()
							  .map(SensorData::getSensor)
							  .distinct()
							  .map(sensor -> new AlarmResponse(sensor, sensorStation))
							  .toList();
		this.gardener = sensorStation.getGardener();
		this.unlocked = sensorStation.isUnlocked();
		this.connected = sensorStation.isConnected();
		this.deleted = sensorStation.isDeleted();
	}

	@Getter
	public static class AlarmResponse implements Serializable {
		private final Sensor sensor;
		private final String alarm;

		public AlarmResponse(Sensor sensor, SensorStation sensorStation) {
			this.sensor = sensor;
			this.alarm = sensorStation.getSensorData()
								 .stream()
								 .filter(d -> d.getSensor().equals(sensor))
								 .max(Comparator.comparing(SensorData::getTimeStamp))
								 .map(SensorData::getAlarm)
								 .orElse("n");
		}
	}
}
