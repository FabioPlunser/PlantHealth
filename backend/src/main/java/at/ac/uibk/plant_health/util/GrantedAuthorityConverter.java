package at.ac.uibk.plant_health.util;

import org.springframework.security.core.GrantedAuthority;

import at.ac.uibk.plant_health.models.user.Permission;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class GrantedAuthorityConverter implements AttributeConverter<GrantedAuthority, String> {
	@Override
	public String convertToDatabaseColumn(GrantedAuthority attribute) {
		return attribute.getAuthority();
	}

	@Override
	public GrantedAuthority convertToEntityAttribute(String dbData) {
		return Permission.valueOf(dbData);
	}
}