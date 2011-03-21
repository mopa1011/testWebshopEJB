package de.webshop.test;

import java.util.Locale;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.BeforeClass;
import org.junit.Rule;
import static org.junit.Assert.fail;
import org.junit.rules.ExpectedException;

import de.webshop.test.util.ArchiveUtil;
import de.webshop.test.util.DbReloadProvider;

public abstract class AbstractTest {

	protected static final Locale LOCALE_DE = Locale.GERMAN;
	protected static final Locale LOCALE_EN = Locale.ENGLISH;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Deployment
	public static EnterpriseArchive createTestArchive() {
		return ArchiveUtil.getTestArchive();
	}
	
	/**
	 */
	@BeforeClass
	public static void reloadDB() {
		try {
			DbReloadProvider.reload();
		}
		catch (Exception e) {
			e.printStackTrace();
			fail("Fehler beim Neuladen der DB: " + e.getClass().getName());
		}
	}
}
