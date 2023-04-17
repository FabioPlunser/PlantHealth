package at.ac.uibk.plant_health.controllers.dash_board_controller;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorData;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.service.PersonSensorStationReferenceService;
import at.ac.uibk.plant_health.service.PersonService;
import at.ac.uibk.plant_health.service.SensorStationService;
import at.ac.uibk.plant_health.util.AuthGenerator;
import at.ac.uibk.plant_health.util.MockAuthContext;
import at.ac.uibk.plant_health.util.StringGenerator;
import org.hamcrest.Matchers;
import org.hamcrest.core.AnyOf;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import at.ac.uibk.plant_health.util.LocalDateTimeJsonParser;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TestDashBoardController {
    @Autowired
    private PersonService personService;
    @Autowired
    private SensorStationService sensorStationService;
    @Autowired
    private PersonSensorStationReferenceService personSensorStationReferenceService;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    private static final int TIME_TOLERANCE = 1;

    private Person createUserAndLogin(boolean alsoAdmin) {
        String username = StringGenerator.username();
        String password = StringGenerator.password();
        Set<GrantedAuthority> permissions = new java.util.HashSet<>(Set.of(Permission.USER));
        if (alsoAdmin) {
            permissions.add(Permission.ADMIN);
        }
        Person person = new Person(username, StringGenerator.email(), password, permissions);
        assertTrue(personService.create(person), "Unable to create user");
        return (Person
                ) MockAuthContext.setLoggedInUser(personService.login(username, password).orElse(null));
    }

    @Test
    public void addSensorStationsToDashboard() throws Exception {
        Person person = createUserAndLogin(false);
        SensorStation s1 = new SensorStation(StringGenerator.macAddress(), 1);
        SensorStation s2 = new SensorStation(StringGenerator.macAddress(), 2);

        s1.setName("SensorStation 1");
        s2.setName("SensorStation 2");

        sensorStationService.save(s1);
        sensorStationService.save(s2);

        personSensorStationReferenceService.addPlantToDashboard(person, s1);
        personSensorStationReferenceService.addPlantToDashboard(person, s2);

        AnyOf<String> nameMatcher = Matchers.anyOf(Matchers.equalTo(s1.getName()), Matchers.equalTo(s2.getName()));

        mockMvc.perform(MockMvcRequestBuilders.get("/get-dashboard-data")
                        .header(HttpHeaders.USER_AGENT, "MockTests")
                        .header(HttpHeaders.AUTHORIZATION,
                                AuthGenerator.generateToken(person))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.plants").isArray(),

                        jsonPath("$.plants[0]").exists(),
                        jsonPath("$.plants[1]").exists(),
                        jsonPath("$.plants[2]").doesNotExist(),

                        jsonPath("$.plants[0].plant-name").value(nameMatcher),
                        jsonPath("$.plants[1].plant-name").value(nameMatcher),

                        jsonPath("$.plants[0].values").isArray(),
                        jsonPath("$.plants[0].values").isEmpty(),

                        jsonPath("$.plants[1].values").isArray(),
                        jsonPath("$.plants[1].values").isEmpty()
                );
    }

    @Test
    public void getDataFromDashboard() throws Exception {
        Person person = createUserAndLogin(false);
        SensorStation s1 = new SensorStation(StringGenerator.macAddress(), 1);

        s1.setName("SensorStation 1");
        sensorStationService.save(s1);
        personSensorStationReferenceService.addPlantToDashboard(person, s1);

        Sensor sensor = new Sensor("Test");
        SensorData data1 = new SensorData(
                LocalDateTime.now(), 1, false, false, sensor
        );
        SensorData data2 = new SensorData(
                data1.getTimeStamp().plusMinutes(5), 2, false, false, sensor
        );

        sensorStationService.addSensorData(s1, data1);
        sensorStationService.addSensorData(s1, data2);

        mockMvc.perform(MockMvcRequestBuilders.get("/get-dashboard-data")
                        .header(HttpHeaders.USER_AGENT, "MockTests")
                        .header(HttpHeaders.AUTHORIZATION,
                                AuthGenerator.generateToken(person))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.plants").isArray(),

                        jsonPath("$.plants[0]").exists(),
                        jsonPath("$.plants[1]").doesNotExist(),

                        jsonPath("$.plants[0].plant-name").value(Matchers.equalTo(s1.getName())),

                        jsonPath("$.plants[0].values").isArray(),
                        jsonPath("$.plants[0].values[0]").exists(),
                        jsonPath("$.plants[0].values[1]").exists(),
                        jsonPath("$.plants[0].values[2]").doesNotExist(),

                        jsonPath("$.plants[0].values[0].timestamp").value(
                                LocalDateTimeJsonParser.equalsWithTolerance(data1.getTimeStamp(), Duration.ofSeconds(TIME_TOLERANCE))
                        ),
                        jsonPath("$.plants[0].values[1].timestamp").value(
                                LocalDateTimeJsonParser.equalsWithTolerance(data2.getTimeStamp(), Duration.ofSeconds(TIME_TOLERANCE))
                        ),

                        jsonPath("$.plants[0].values[0].sensors").isArray(),
                        jsonPath("$.plants[0].values[0].sensors[0]").exists(),
                        jsonPath("$.plants[0].values[0].sensors[1]").doesNotExist(),
                        jsonPath("$.plants[0].values[1].sensors[0]").exists(),
                        jsonPath("$.plants[0].values[1].sensors[1]").doesNotExist(),

                        jsonPath("$.plants[0].values[0].sensors[0].sensor").value(Matchers.equalTo(sensor.getType())),
                        jsonPath("$.plants[0].values[0].sensors[0].value").value(Matchers.equalTo(data1.getValue())),
                        // TODO jsonPath("$.plants[0].values[0].sensors[0].unit").value(Matchers.equalTo(sensor.getUnit())),
                        jsonPath("$.plants[0].values[0].sensors[0].alarm").value(Matchers.equalTo(Character.toString(data1.getAlarm()))),

                        jsonPath("$.plants[0].values[1].sensors[0].sensor").value(Matchers.equalTo(sensor.getType())),
                        jsonPath("$.plants[0].values[1].sensors[0].value").value(Matchers.equalTo(data2.getValue())),
                        // TODO jsonPath("$.plants[0].values[1].sensors[0].unit").value(Matchers.equalTo(sensor.getUnit())),
                        jsonPath("$.plants[0].values[1].sensors[0].alarm").value(Matchers.equalTo(Character.toString(data1.getAlarm())))
                );
    }
}
