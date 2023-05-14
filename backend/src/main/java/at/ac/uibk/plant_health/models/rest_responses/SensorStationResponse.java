package at.ac.uibk.plant_health.models.rest_responses;

import java.awt.image.ImageProducer;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import at.ac.uibk.plant_health.models.SensorStationPersonReference;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorData;
import at.ac.uibk.plant_health.models.plant.SensorLimits;
import at.ac.uibk.plant_health.models.plant.SensorStationPicture;
import at.ac.uibk.plant_health.models.user.Person;
import lombok.Getter;

@Getter
public class SensorStationResponse extends RestResponse implements Serializable {
	private final InnerResponse sensorStation;

	public SensorStationResponse(SensorStation sensorStation, Person person) {
		super();
		this.sensorStation = new InnerResponse(sensorStation, person);
	}

	@Getter
	private static class InnerResponse {
		private final String bdAddress;
		private final int dipSwitchId;
		private final String name;
		private final String roomName;
		private final UUID sensorStationId;
		private final Person gardener;
		private final boolean unlocked;
		private final boolean connected;
		private final boolean deleted;
		private final List<SensorLimitsResponse> sensorLimits;
		private final List<SensorStationPersonReference> sensorStationPersonReferences;
		private final List<SensorStationPicture> sensorStationPictures;

		public InnerResponse(SensorStation sensorStation, Person person) {
			this.bdAddress = sensorStation.getBdAddress();
			this.dipSwitchId = sensorStation.getDipSwitchId();
			this.name = sensorStation.getName();
			var access_point = sensorStation.getAccessPoint();
			if (access_point != null) this.roomName = access_point.getRoomName(); else this.roomName = null;
			this.sensorStationId = sensorStation.getDeviceId();
			this.gardener = sensorStation.getGardener();
			this.unlocked = sensorStation.isUnlocked();
			this.connected = sensorStation.isConnected();
			this.deleted = sensorStation.isDeleted();

			this.sensorLimits =
					sensorStation.getSensorData()
							.stream()
							.map(SensorData::getSensor)
							.distinct()
							.map(sensor -> {
								Optional<SensorLimits> newestLimit =
										sensorStation.getSensorLimits()
												.stream()
												.filter(slr -> slr.getSensor().equals(sensor))
												.max(Comparator.comparing(SensorLimits::getTimeStamp
												));

								return newestLimit
										.map(limits -> new SensorLimitsResponse(limits, person))
										.orElseGet(
												()
														-> new SensorLimitsResponse(
																new SensorLimits(
																		LocalDateTime.now(), 0, 0,
																		0, sensor, person,
																		sensorStation
																),
																person
														)
										);
							})
							.toList();
			this.sensorStationPersonReferences = sensorStation.getSensorStationPersonReferences();
			this.sensorStationPictures = sensorStation.getSensorStationPictures();
		}

		@Getter
		private static class SensorLimitsResponse {
			private final LocalDateTime timeStamp;
			private final float upperLimit;
			private final float lowerLimit;
			private final int thresholdDuration;
			private final Sensor sensor;
			private final Person gardener;
			private final boolean deleted;

			public SensorLimitsResponse(SensorLimits sensorLimit, Person person) {
				this.timeStamp = sensorLimit.getTimeStamp();
				this.upperLimit = sensorLimit.getUpperLimit();
				this.lowerLimit = sensorLimit.getLowerLimit();
				this.thresholdDuration = sensorLimit.getThresholdDuration();
				this.sensor = sensorLimit.getSensor();
				this.gardener = sensorLimit.getGardener();
				this.deleted = sensorLimit.isDeleted();
			}
		}
	}
}
