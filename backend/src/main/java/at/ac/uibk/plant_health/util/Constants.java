package at.ac.uibk.plant_health.util;

public class Constants {
	/**
	 * Aspects are ordered:<br/>
	 * - Logging happens first, so all Requests are logged.<br/>
	 * - Afterwards Permissions are checked.<br/>
	 * - At the End, Parameters are injected.<br/>
	 */
	public static final int LOGGING_ASPECT_ORDER = 0;
	public static final int PERMISSION_CHECKING_ASPECT_ORDER = 100;
	public static final int INJECTION_ASPECT_ORDER = 200;
}
