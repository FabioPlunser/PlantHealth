package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import at.ac.uibk.plant_health.models.SensorStationPersonReference;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorData;
import at.ac.uibk.plant_health.models.plant.SensorLimits;
import at.ac.uibk.plant_health.models.plant.SensorStationPicture;
import at.ac.uibk.plant_health.models.user.Person;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class SensorStationDetailResponse extends RestResponse implements Serializable {
	private final SensorStationInnerResponse sensorStation;

	public SensorStationDetailResponse(SensorStation sensorStation, Person person) {
		this.sensorStation = new SensorStationInnerResponse(sensorStation, person);
	}

	@Getter
	private static class SensorStationInnerResponse
			extends SensorStationBaseResponse implements Serializable {
		private final List<SensorLimitsResponse> sensorLimits;
		private final List<SensorStationPersonReference> sensorStationPersonReferences;
		private final List<SensorStationPicture> sensorStationPictures;

		public SensorStationInnerResponse(SensorStation sensorStation, Person person) {
			super(sensorStation);
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

								return newestLimit.map(SensorLimitsResponse::new)
										.orElseGet(
												()
														-> new SensorLimitsResponse(
																new SensorLimits(
																		LocalDateTime.now(), 0, 0,
																		0, sensor, person,
																		sensorStation
																)
														)
										);
							})
							.toList();
			this.sensorStationPersonReferences = sensorStation.getSensorStationPersonReferences();
			this.sensorStationPictures = sensorStation.getSensorStationPictures();
		}

		@Getter
		private static class SensorLimitsResponse implements Serializable {
			private final LocalDateTime timeStamp;
			private final float upperLimit;
			private final float lowerLimit;
			private final int thresholdDuration;
			private final Sensor sensor;
			private final Person gardener;
			private final boolean deleted;

			public SensorLimitsResponse(SensorLimits sensorLimit) {
				this.timeStamp = sensorLimit.getTimeStamp();
				this.upperLimit = sensorLimit.getUpperLimit();
				this.lowerLimit = sensorLimit.getLowerLimit();
				this.thresholdDuration = sensorLimit.getThresholdDuration();
				this.sensor = sensorLimit.getSensor();
				this.gardener = sensorLimit.getGardener();
				this.deleted = sensorLimit.isDeleted();
			}
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
}
