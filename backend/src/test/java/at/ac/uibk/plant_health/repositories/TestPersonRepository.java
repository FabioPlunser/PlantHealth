package at.ac.uibk.plant_health.repositories;

import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.service.PersonService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
public class TestPersonRepository {
    @Autowired
    private PersonRepository personRepository;

    @Test
    public void testUpdateToken() {
        Person person = new Person("Test-User", "test@planthealth.at", "password");

        personRepository.save(person);
        person.setToken(null);

        Assertions.assertNull(person.getToken());
        Assertions.assertNull(person.getTokenCreationDate());

        Assertions.assertNotNull(personRepository.updateToken(person));

        person = personRepository.findById(person.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Assertions.assertNull(person.getToken());
        Assertions.assertNull(person.getTokenCreationDate());

        var token = UUID.randomUUID();
        person.setToken(token);
        var tokenCreationDate = person.getTokenCreationDate();

        Assertions.assertNotNull(personRepository.updateToken(person));

        person = personRepository.findById(person.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Person finalPerson = person;
        Assertions.assertAll(
                () -> Assertions.assertNotNull(finalPerson.getToken()),
                () -> Assertions.assertNotNull(finalPerson.getTokenCreationDate()),
                () -> Assertions.assertEquals(finalPerson.getToken(), token),
                () -> Assertions.assertEquals(
                        finalPerson.getTokenCreationDate().truncatedTo(ChronoUnit.SECONDS),
                        tokenCreationDate.truncatedTo(ChronoUnit.SECONDS)
                )
        );
    }
}
