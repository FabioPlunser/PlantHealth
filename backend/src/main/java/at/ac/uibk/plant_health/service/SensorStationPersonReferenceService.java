package at.ac.uibk.plant_health.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import at.ac.uibk.plant_health.models.SensorStationPersonReference;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.exceptions.ServiceException;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.repositories.PersonRepository;
import at.ac.uibk.plant_health.repositories.SensorStationPersonReferenceRepository;
import at.ac.uibk.plant_health.repositories.SensorStationRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SensorStationPersonReferenceService {
	@Autowired
	private SensorStationPersonReferenceRepository sensorStationPersonReferenceRepository;
	@Autowired
	private PersonService personService;
	@Autowired
	private SensorStationService sensorStationService;
	@Autowired
	private PersonRepository personRepository;
	@Autowired
	private SensorStationRepository sensorStationRepository;

	//	@Transactional
	public void addPlantToDashboard(Person person, SensorStation sensorStation)
			throws ServiceException {
		if (person == null || sensorStation == null)
			throw new ServiceException("Invalid input", 400);

		try {
			// check if sensor station is already in dashboard
			var maybeSensorStationPersonReference =
					sensorStationPersonReferenceRepository.findByPersonAndSensorStation(
							person, sensorStation
					);
			if (maybeSensorStationPersonReference.isPresent())
				throw new ServiceException("Sensor station already in dashboard", 400);

			SensorStationPersonReference reference =
					new SensorStationPersonReference(sensorStation, person, false, true);

			Optional<Person> maybePerson = personService.findById(person.getPersonId());
			if (maybePerson.isEmpty()) throw new ServiceException("Could not find person", 404);
			person = maybePerson.get();
			person.addSensorStationReference(reference);
			//			personService.save(person);

			SensorStation sensorStation1 =
					sensorStationService.findById(sensorStation.getDeviceId());
			sensorStation1.addSensorStationReference(reference);
			//			sensorStationService.save(sensorStation);

			sensorStationPersonReferenceRepository.save(reference);

		} catch (Exception e) {
			log.error("Could not add sensor station to dashboard", e);
			throw new ServiceException("Could not add sensor station to dashboard", 500);
		}
	}

	//	@Transactional
	public void removePlantFromDashboard(Person person, SensorStation sensorStation)
			throws ServiceException {
		if (person == null || sensorStation == null)
			throw new ServiceException("Invalid input", 400);
		try {
			// check if sensor station is in dashboard
			var maybeSensorStationPersonReference =
					sensorStationPersonReferenceRepository.findByPersonAndSensorStation(
							person, sensorStation
					);
			if (maybeSensorStationPersonReference.isEmpty())
				throw new ServiceException("Sensor station not in dashboard", 400);

			Optional<Person> maybePerson = personService.findById(person.getPersonId());
			if (maybePerson.isEmpty()) throw new ServiceException("Could not find person", 404);
			person = maybePerson.get();
			sensorStationService.findById(sensorStation.getDeviceId());
			sensorStationPersonReferenceRepository.deleteByPersonAndSensorStation(
					person, sensorStation
			);
		} catch (Exception e) {
			log.error("Could not remove sensor station from dashboard", e);
			throw new ServiceException("Could not remove sensor station from dashboard", 500);
		}
		//		var maybePerson = personRepository.findById(person.getPersonId());
		//		if (maybePerson.isEmpty()) return false;
		//		var maybeSensorStation =
		// sensorStationRepository.findById(sensorStation.getDeviceId()); 		if
		//(maybeSensorStation.isEmpty()) return false;
		//
		//		person = maybePerson.get();
		//		sensorStation = maybeSensorStation.get();
		//
		//		return sensorStationPersonReferenceRepository.deleteByPersonAndSensorStation(person,
		// sensorStation) >= 1;
	}
}
