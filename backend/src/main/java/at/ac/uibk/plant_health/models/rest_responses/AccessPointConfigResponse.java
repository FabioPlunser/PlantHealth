package at.ac.uibk.plant_health.models.rest_responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorLimits;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.MODULE)
public class AccessPointConfigResponse extends RestResponse implements Serializable {
	private final String roomName;
	private final boolean pairingMode;
	private final int transferInterval;
	private final List<SensorStationInfo> sensorStations;

	public AccessPointConfigResponse(AccessPoint accessPoint) {
		this.roomName = accessPoint.getRoomName();
		this.pairingMode = accessPoint.getScanActive();
		this.transferInterval = accessPoint.getTransferInterval();
		this.sensorStations =
				accessPoint.getSensorStations()
						.stream()
						.filter(station -> station.isDeleted() && station.isUnlocked())
						.map(SensorStationInfo::new)
						.toList();
	}

	@Getter
	public static class SensorStationInfo implements Serializable {
		private final String bdAddress;
		private final List<SensorInfo> sensors;

		public SensorStationInfo(SensorStation sensorStation) {
			this.bdAddress = sensorStation.getBdAddress();
			this.sensors = sensorStation.getSensorLimits().stream().map(SensorInfo::new).toList();
		}

		@Getter
		public static class SensorInfo implements Serializable {
			private final String sensorName;
			private final Limits limits;
			private final int alarmThresholdTime;

			public SensorInfo(SensorLimits sensorLimits) {
				this.sensorName = sensorLimits.getSensor().getType();
				this.limits =
						new Limits(sensorLimits.getLowerLimit(), sensorLimits.getUpperLimit());
				this.alarmThresholdTime = sensorLimits.getThresholdDuration();
			}

			@Getter
			public static class Limits implements Serializable {
				private final double lowerLimit;
				private final double upperLimit;

				public Limits(double lowerLimit, double upperLimit) {
					this.lowerLimit = lowerLimit;
					this.upperLimit = upperLimit;
				}
			}
		}
	}
}
