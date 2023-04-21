package at.ac.uibk.plant_health.models.user;

import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

public enum Permission implements GrantedAuthority {
	USER,
	GARDENER,
	ADMIN;

	public static Set<Permission> defaultPermissions() {
		return Set.of(USER);
	}

	public static Set<GrantedAuthority> defaultAuthorities() {
		return Set.of(USER);
	}

	public static Set<Permission> adminPermissions() {
		return Set.of(ADMIN);
	}

	public static Set<GrantedAuthority> adminAuthorities() {
		return Set.of(ADMIN);
	}

	private static final Set<Permission> ALL_PERMISSIONS = Set.of(Permission.values());

	public static Set<Permission> allPermissions() {
		return ALL_PERMISSIONS;
	}

	public static Set<GrantedAuthority> allAuthorities() {
		return (Set) ALL_PERMISSIONS;
	}

	@Override
	public String getAuthority() {
		return this.toString();
	}
}
