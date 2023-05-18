package at.ac.uibk.plant_health.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import at.ac.uibk.plant_health.models.SensorStationPersonReference;
import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorData;
import at.ac.uibk.plant_health.models.plant.SensorLimits;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.repositories.*;
import at.ac.uibk.plant_health.service.PersonService;
import at.ac.uibk.plant_health.service.SensorStationService;

@Component
@Profile("!prod & !test & !Prod & !Test & !PROD & !TEST")
public class DumpConfig {
	@Autowired
	private PersonService personService;
	@Autowired
	private AccessPointRepository accessPointRepository;
	@Autowired
	private SensorStationRepository sensorStationRepository;
	@Autowired
	private SensorStationPersonReferenceRepository sensorStationPersonReferenceRepository;
	@Autowired
	private SensorRepository sensorRepository;
	@Autowired
	private SensorLimitsRepository sensorLimitsRepository;
	@Autowired
	private SensorDataRepository sensorDataRepository;

	@EventListener(ApplicationReadyEvent.class)
	public void loadDatabaseDump() {
		int numAdmins = 1;
		int numGardeners = 3;
		int numUsers = 5;
		int numAccessPoints = 2;
		int numSensorStations = 8;
		int numSensorData = 500;

		final String defaultPassword = "password";
		final String emailSuffix = "@planthealth.at";

		List<Person> admins =
				IntStream.range(0, numAdmins)
						.mapToObj(
								i
								-> new Person(
										"Admin_" + i, "admin" + i + emailSuffix,
										defaultPassword, Set.of(Permission.ADMIN)
								)
						)
						.toList();
		List<Person> gardeners =
				IntStream.range(0, numGardeners)
						.mapToObj(
								i
								-> new Person(
										"Gardener_" + i, "gardener" + i + emailSuffix,
										defaultPassword, Set.of(Permission.GARDENER)
								)
						)
						.toList();
		List<Person> users =
				IntStream.range(0, numUsers)
						.mapToObj(
								i
								-> new Person(
										"User_" + i, "user" + i + emailSuffix, defaultPassword,
										Set.of(Permission.USER)
								)
						)
						.toList();
		admins.forEach(personService::save);
		gardeners.forEach(personService::save);
		users.forEach(personService::save);

		var aps = IntStream.range(0, numAccessPoints)
						  .mapToObj(i -> {
							  var ap = new AccessPoint(
									  UUID.randomUUID(), "access_point_" + i, 60 + i, false
							  );

							  ap.setConnected((i & 0x1) > 0);
							  ap.setUnlocked(true);
							  ap.setDeleted(false);

							  return ap;
						  })
						  .toList();
		accessPointRepository.saveAll(aps);

		List<Pair<String, String>> sensorKeyValues =
				List.of(Pair.of("Temperature", "°C"), Pair.of("Air Humidity", "%"),
						Pair.of("Air Pressure", "hPa"), Pair.of("Earth Humidity", "%"),
						Pair.of("Light Intensity", "lm"), Pair.of("Air Quality", "%"),
						Pair.of("Battery Level", "%"));
		var sensors = sensorKeyValues.stream()
							  .map(p -> new Sensor(p.getFirst(), p.getSecond()))
							  .toList();
		sensorRepository.saveAll(sensors);

		var sensorStations =
				IntStream.range(0, numSensorStations)
						.mapToObj(i -> {
							var s = new SensorStation(macAddress(), i);

							s.setName("SensorStation_" + i);

							var refs =
									Stream.concat(IntStream.range(0, users.size())
														  .filter(j -> (i % 3) == (j % 3))
														  .mapToObj(
																  j
																  -> new SensorStationPersonReference(
																		  s, users.get(j),
																		  false, true
																  )
														  ),
												  IntStream.range(0, gardeners.size())
														  .filter(j -> j == i)
														  .mapToObj(
																  j
																  -> new SensorStationPersonReference(
																		  s, gardeners.get(j),
																		  true, (j & 0x1) > 0
																  )
														  ))
											.toList();
							s.setSensorStationPersonReferences(refs);

							s.setSensorLimits(
									sensors.stream()
											.map(sensor -> {
												var sl = new SensorLimits(
														LocalDateTime.now(),
														(float)(0.9 * numSensorData) + 100,
														(float)(0.1 * numSensorData) + 100,
														60, sensor,
														gardeners.get(i % gardeners.size()), s
												);
												sl.setDeleted(false);
												return sl;
											})
											.toList()
							);

							var sensorData =
									IntStream.range(0, numSensorData)
											.mapToObj(j -> {
												boolean above = j > 0.9 * numSensorData;
												boolean below = j < 0.1 * numSensorData;
												String alarm;
												if (above) {
													alarm = "h";
												} else if(below) {
													alarm = "l";
												} else{
													alarm = "n";
												}
												return Stream.of(new SensorData(
														LocalDateTime.now(), (float)100 + j, alarm,
														sensors.get(j % sensors.size()), s
												));
											})
											.flatMap(Function.identity())
											.toList();
							s.setSensorData(sensorData);

							s.setConnected((i & 0x1) > 0);
							s.setUnlocked((i & 0x2) > 0);
							s.setDeleted(false);

							s.setAccessPoint(aps.get(i % numAccessPoints));

							return s;
						})
						.toList();

		sensorStationRepository.saveAll(sensorStations);
		sensorStations.forEach(s -> sensorLimitsRepository.saveAll(s.getSensorLimits()));
		sensorStations.forEach(s -> sensorDataRepository.saveAll(s.getSensorData()));
		sensorStations.forEach(
				s
				-> sensorStationPersonReferenceRepository.saveAll(s.getSensorStationPersonReferences())
		);
	}

	private static final String CAPITAL_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String SMALL_LETTERS = "abcdefghijklmnopqrstuvwxyz";
	private static final String NUMBERS = "0123456789";

	public static String macAddress() {
		int macAddressLength = 17;
		return base(SMALL_LETTERS + CAPITAL_LETTERS + NUMBERS, macAddressLength);
	}

	private static final SecureRandom random = new SecureRandom();

	public static String base(String alphabet, int n) {
		StringBuilder sb = new StringBuilder(n);
		for (int i = 0; i < n; i++) {
			int index = random.nextInt(0, alphabet.length());
			sb.append(alphabet.charAt(index));
		}
		return sb.toString();
	}
}
