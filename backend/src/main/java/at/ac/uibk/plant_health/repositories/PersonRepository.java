package at.ac.uibk.plant_health.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import at.ac.uibk.plant_health.models.Log;
import at.ac.uibk.plant_health.models.annotations.AuditLogAnnotation;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;

public interface PersonRepository extends CrudRepository<Person, UUID> {
	@Override
	List<Person> findAll();

	List<Person> findAllByPermissionsIsContaining(Permission permission);

	Optional<Person> findByUsernameAndToken(String username, UUID token);

	Optional<Person> findByUsername(String username);

	@Transactional
	default<S extends Person> S updateToken(S person) {
		return updateToken(person.getPersonId(), person.getToken(), person.getTokenCreationDate())
						== 1
				? person
				: null;
	}

	@Transactional
	@Modifying
	@Query("update Person p set p.token = :token, p.tokenCreationDate = :tokenCreationDate where p.id = :id"
	)
	int
	updateToken(
			@Param("id") UUID id, @Param("token") UUID token,
			@Param("tokenCreationDate") LocalDateTime tokenCreationDate
	);

	@Transactional
	@Modifying
	@Query("update Person p set p.username = :username, p.permissions = :permissions where p.id = :id"
	)
	int
	updateUserDetails(
			@Param("id") UUID id, @Param("username") String username,
			@Param("permissions") Set<GrantedAuthority> permissions
	);
}