package at.ac.uibk.plant_health.util;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

import org.h2.tools.Server;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Anotate tests where the H2 Console should be available via localhost:3000
 * with @ExtendWith({SetupH2Console.class})
 */
public class SetupH2Console implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
	private static boolean started = false;

	/**
	 * BeforeAll
	 * @param context
	 */
	@Override
	public void beforeAll(ExtensionContext context) throws SQLException {
		if (!started) {
			started = true;
			Server.createWebServer("-web", "-webAllowOthers", "-webPort", "3000").start();
			context.getRoot().getStore(GLOBAL).put(UUID.randomUUID(), this);
		}
	}

	/**
	 * AfterAll
	 */
	@Override
	public void close() {}
}
