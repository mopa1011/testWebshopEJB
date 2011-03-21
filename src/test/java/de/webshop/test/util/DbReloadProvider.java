package de.webshop.test.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * DbReloadProvider ist eine Hilfsklasse, um das Neuladen der DB zu starten:
 * Es wird &uuml;berpr&uuml;ft, ob der ClassLoader von JBoss oder von z.B. Sun ist.
 *
 * Wenn der ClassLoader nicht von JBoss ist, dann wird DbReloadProvider beim eigentlichen 
 * Start der JUnit-Tests aufgerufen und das Neuladen der DB kann erfolgen.
 *
 * Wenn der ClassLoader von JBoss ist, dann wird DbReloadProvider das zweite Mal
 * aufgerufen - also nachdem die JUnit-Tests in den JBoss geladen sind.
 * Jetzt wird die DB nicht mehr neu geladen, weil es in JBoss nicht dbunit.jar
 * gibt und weil die DB bereits beim eigentlichen Start der JUnit-Tests neu
 * geladen wurde.
 */
public abstract class DbReloadProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(DbReloadProvider.class);
	private static final String IMPL_CLASS = "de.webshop.test.util.DbReloadImpl";

	/**
	 */
	public static void reload() throws Exception {
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final String classnameClassLoader = classLoader.getClass().getName();
		
		LOGGER.info("Trying database reload...");
		if (classnameClassLoader.startsWith("org.jboss")) {
			// Classloader von JBoss, d.h. *kein* Neuladen der DB, da sie bereits
			// beim normalen Start von JUnit ueber den Classloader von Sun (s.o.)
			// geladen wurde
			LOGGER.info("...no database reload in JBoss");
			return;
		}

		// "Normaler" Classloader von z.B. Sun und *NICHT* von JBoss, d.h. Neuladen der DB
		final Class<?> clazz = Class.forName(IMPL_CLASS);
		final Class<? extends DbReload> dbReloadImplClass = clazz.asSubclass(DbReload.class);
		final DbReload dbReload = dbReloadImplClass.newInstance();
		dbReload.reload();
		LOGGER.info("...succeeded");
	}
}
