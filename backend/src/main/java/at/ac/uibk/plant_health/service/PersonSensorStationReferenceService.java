package at.ac.uibk.plant_health.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import at.ac.uibk.plant_health.models.SensorStationPersonReference;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.repositories.PersonRepository;
import at.ac.uibk.plant_health.repositories.PlantPersonReferenceRepository;
import at.ac.uibk.plant_health.repositories.SensorStationRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PersonSensorStationReferenceService {
	@Autowired
	private PlantPersonReferenceRepository plantPersonReferenceRepository;
	@Autowired
	private PersonRepository personRepository;
	@Autowired
	private SensorStationRepository sensorStationRepository;

	@Transactional
	public boolean addPlantToDashboard(Person person, SensorStation sensorStation) {
		if (person == null || sensorStation == null) return false;

		var maybePerson = personRepository.findById(person.getPersonId());
		if (maybePerson.isEmpty()) return false;
		var maybeSensorStation = sensorStationRepository.findById(sensorStation.getDeviceId());
		if (maybeSensorStation.isEmpty()) return false;

		person = maybePerson.get();
		sensorStation = maybeSensorStation.get();

		SensorStationPersonReference s =
				new SensorStationPersonReference(sensorStation, person, false, true);

		person.addSensorStationReference(s);
		sensorStation.addSensorStationReference(s);

		return plantPersonReferenceRepository.save(s) != null;
	}
}
