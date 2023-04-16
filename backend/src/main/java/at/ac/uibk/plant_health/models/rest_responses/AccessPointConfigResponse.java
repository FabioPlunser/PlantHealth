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
@NoArgsConstructor(access = AccessLevel.MODULE)
@AllArgsConstructor(access = AccessLevel.MODULE)
public class AccessPointConfigResponse extends RestResponse implements Serializable {
	private String roomName;
	private boolean pairingMode;
	private int transferInterval;
	private List<SensorStationInfo> sensorStations;

	public AccessPointConfigResponse(AccessPoint accessPoint) {
		System.out.println(accessPoint);
		this.roomName = accessPoint.getRoomName();
		this.pairingMode = accessPoint.isScanActive();
		this.transferInterval = accessPoint.getTransferInterval();
		this.sensorStations =
				accessPoint.getSensorStations().stream().map(SensorStationInfo::new).toList();
	}

	@Getter
	public static class SensorStationInfo implements Serializable {
		private String bdAddress;
		private List<SensorInfo> sensors;

		public SensorStationInfo(SensorStation sensorStation) {
			this.bdAddress = sensorStation.getBdAddress();
			this.sensors = sensorStation.getSensorLimits().stream().map(SensorInfo::new).toList();
		}

		@Getter
		public static class SensorInfo implements Serializable {
			private String sensorName;
			private Limits limits;
			private int alarmThresholdTime;

			public SensorInfo(SensorLimits sensorLimits) {
				this.sensorName = sensorLimits.getSensor().getType().name();
				this.limits =
						new Limits(sensorLimits.getLowerLimit(), sensorLimits.getUpperLimit());
				this.alarmThresholdTime = sensorLimits.getThresholdDuration();
			}

			@Getter
			public static class Limits implements Serializable {
				private double lowerLimit;
				private double upperLimit;

				public Limits(double lowerLimit, double upperLimit) {
					this.lowerLimit = lowerLimit;
					this.upperLimit = upperLimit;
				}
			}
		}
	}
}
