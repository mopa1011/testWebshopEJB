package de.webshop.test;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.ejb.EJB;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.webshop.artikelverwaltung.domain.Artikel;
import de.webshop.artikelverwaltung.domain.Produktbewertung;
import de.webshop.artikelverwaltung.service.ArtikelDuplikatException;
import de.webshop.artikelverwaltung.service.ArtikelValidationException;
import de.webshop.artikelverwaltung.service.Artikelverwaltung;
import de.webshop.artikelverwaltung.service.InvalidArtikelIdException;
import de.webshop.artikelverwaltung.service.InvalidProduktbewertungIdException;
import de.webshop.benutzerverwaltung.domain.AbstractBenutzer;
import de.webshop.benutzerverwaltung.domain.Lieferadresse;
import de.webshop.benutzerverwaltung.domain.Rechnungsadresse;
import de.webshop.benutzerverwaltung.domain.Rolle;
import de.webshop.benutzerverwaltung.domain.Rolle.RolleTyp;
import de.webshop.benutzerverwaltung.domain.RolleHasBenutzer;
import de.webshop.benutzerverwaltung.service.BenutzerDuplikatException;
import de.webshop.benutzerverwaltung.service.BenutzerValidationException;
import de.webshop.benutzerverwaltung.service.Benutzerverwaltung;
import de.webshop.benutzerverwaltung.service.InvalidBenutzerIdException;
import de.webshop.benutzerverwaltung.service.InvalidEmailException;
import de.webshop.benutzerverwaltung.service.InvalidNachnameException;
import de.webshop.benutzerverwaltung.service.InvalidRolleException;
import de.webshop.benutzerverwaltung.service.InvalidRolleIdException;
import de.webshop.benutzerverwaltung.service.ProduktbewertungDuplikatException;
import de.webshop.benutzerverwaltung.service.ProduktbewertungValidationException;
import de.webshop.benutzerverwaltung.service.RolleHasBenutzerDuplikatException;
import de.webshop.benutzerverwaltung.service.RolleHasBenutzerValidationException;
import de.webshop.util.NotFoundException;

@RunWith(Arquillian.class)
public class BenutzerverwaltungTest extends AbstractTest {
	
	private static final Long ID_EXISTS = 2L;
	private static final Long ID_NOT_EXISTS = 1000L;
	private static final Long ID_INVALID = 0L;
	
	private static final String EMAIL_EXISTS = "sast1011@hs-karlsruhe.de";
	private static final String EMAIL_NOT_EXISTS = "x@x.de";
	private static final String EMAIL_INVALID = "bla";
	
	private static final String NACHNAME_EXISTS = "Er";
	private static final String NACHNAME_NOT_EXISTS = "Bauer";
	private static final String NACHNAME_INVALID = "123";
	
	private static final String NACHNAME_NEW = "Muster";
	private static final String VORNAME_NEW = "Max";
	private static final String EMAIL_NEW = "mas@muster.de";
	private static final String PASSWORT_NEW = "abc";
	private static final String KONTONUMMER_NEW = "123456789012";
	
	private static final String PLZ_NEW = "12345";
	private static final String ORT_NEW = "Stadt";
	private static final String STRASSE_NEW = "Strasse";
	private static final String HAUSNUMMER_NEW = "1b";
	
	private static final Long ARTIKEL_ID_EXISTS = 1L;
	private static Long ARTIKEL_ID_NEW;
	private static final Integer PRODUKTBEWERTUNG_BEWERTUNG_NEW = 5;
	private static final String PRODUKTBEWERTUNG_KOMMENTAR_NEW = "Das Produkt is sau geil. Immer wieder!";
	
	@EJB
	private Benutzerverwaltung bv;

	@EJB
	private Artikelverwaltung av;
	
	@Test
	public void findBenutzerByIdFetchAll() throws InvalidBenutzerIdException, NotFoundException {
		AbstractBenutzer stephan = bv.findBenutzerByIDFetchAll(ID_EXISTS, LOCALE_DE);
		
		assertThat(stephan.getRollehasbenutzer().size(), is(1));
		for(RolleHasBenutzer rhb : stephan.getRollehasbenutzer()) {
			assertThat(rhb.getRolle().getRolle(), is(RolleTyp.ADMINISTRATOR));
		}
		
		assertThat(stephan.getProduktbewertungen().size(), is(4));
		assertThat(stephan.getBestellungen().size(), is(2));
		
		assertThat(stephan.getRechnungsadresse().getOrt(), is("Westheim"));
		assertThat(stephan.getLieferadresse().getOrt(), is("Berlin"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void findBenutzerNByNachname() throws InvalidNachnameException, NotFoundException {
		List<AbstractBenutzer> result = bv.findBenutzerNByNachnameFetchBestellungen("%" + NACHNAME_EXISTS + "%", LOCALE_DE, false);
		
		assertThat(result.size(), is(3));
		
		for(AbstractBenutzer a : result) {
			assertThat(a.getNachname(), anyOf(is("Sauer"), is("Thummert"), is("Moser")));
		}
	}
	
	@Test
	public void findBenutzerNByNachnameNichtVorhanden() throws InvalidNachnameException, NotFoundException {
		
		thrown.expect(NotFoundException.class);
		bv.findBenutzerNByNachnameFetchBestellungen(NACHNAME_NOT_EXISTS, LOCALE_DE, true);
	}
	
	@Test
	public void findBenutzerNByNachnameInvalidNachname() throws InvalidNachnameException, NotFoundException {
		
		thrown.expect(InvalidNachnameException.class);
		bv.findBenutzerNByNachnameFetchBestellungen(NACHNAME_INVALID, LOCALE_DE, true);
	}
	
	@Test
	public void findBenutzerN() throws NotFoundException {
		List<AbstractBenutzer> result = bv.findBenutzerN();
		
		assertThat(result.size(), is(5));
	}
	
	@Test
	public void findBenutzerById() throws NotFoundException, InvalidBenutzerIdException {
		
		AbstractBenutzer b = bv.findBenutzerByID(ID_EXISTS, LOCALE_DE);
		
		assertThat(ID_EXISTS, is(b.getIdBenutzer()));
	}
	
	@Test
	public void findBenutzerByIdNichtVorhanden() throws NotFoundException, InvalidBenutzerIdException {
		
		thrown.expect(NotFoundException.class);
		bv.findBenutzerByID(ID_NOT_EXISTS, LOCALE_DE);
	}
	
	@Test
	public void findBenutzerByIdInvalidId() throws NotFoundException, InvalidBenutzerIdException {
		
		thrown.expect(InvalidBenutzerIdException.class);
		bv.findBenutzerByID(ID_INVALID, LOCALE_DE);
	}
	
	@Test
	public void findBenutzerByEmail() throws NotFoundException, InvalidEmailException {
		
		AbstractBenutzer b = bv.findBenutzerByEmail(EMAIL_EXISTS, LOCALE_DE);
		
		assertThat(EMAIL_EXISTS, is(b.getEmail()));
	}
	
	@Test
	public void findBenutzerByEmailNichtVorhanden() throws NotFoundException, InvalidEmailException {
		
		thrown.expect(NotFoundException.class);
		bv.findBenutzerByEmail(EMAIL_NOT_EXISTS, LOCALE_DE);
	}
	
	@Test
	public void findBenutzerByEmailInvalidEmail() throws NotFoundException, InvalidEmailException {
		
		thrown.expect(InvalidEmailException.class);
		bv.findBenutzerByEmail(EMAIL_INVALID, LOCALE_DE);
	}
	
	@Test
	public void createBenutzer() throws BenutzerValidationException, BenutzerDuplikatException, InvalidNachnameException, NotFoundException {
		AbstractBenutzer benutzer = new AbstractBenutzer();
		benutzer.setEmail(EMAIL_NEW);
		benutzer.setNachname(NACHNAME_NEW);
		benutzer.setVorname(VORNAME_NEW);
		benutzer.setPasswort(PASSWORT_NEW);
		benutzer.setPasswortWdh(PASSWORT_NEW);
		
		Rechnungsadresse rechnungsadresse = new Rechnungsadresse();
		rechnungsadresse.setHausnummer(HAUSNUMMER_NEW);
		rechnungsadresse.setOrt(ORT_NEW);
		rechnungsadresse.setPlz(PLZ_NEW);
		rechnungsadresse.setStrasse(STRASSE_NEW);
		rechnungsadresse.setBenutzer(benutzer);
		
		benutzer.setRechnungsadresse(rechnungsadresse);
		
		AbstractBenutzer createdBenutzer = bv.createBenutzer(benutzer, LOCALE_DE, false);
		
		AbstractBenutzer gefundenerBenutzer = bv.findBenutzerNByNachname(NACHNAME_NEW, LOCALE_DE, true).get(0);
		
		assertThat(createdBenutzer.getIdBenutzer(), is(gefundenerBenutzer.getIdBenutzer()));
		assertThat(gefundenerBenutzer.getRechnungsadresse().getHausnummer(), is(HAUSNUMMER_NEW));
	}
	
	@Test
	public void createBenutzerDuplikat() throws BenutzerValidationException, BenutzerDuplikatException {
		AbstractBenutzer benutzer = new AbstractBenutzer();
		benutzer.setEmail(EMAIL_EXISTS);
		benutzer.setNachname(NACHNAME_NEW);
		benutzer.setVorname(VORNAME_NEW);
		benutzer.setPasswort(PASSWORT_NEW);
		benutzer.setPasswortWdh(PASSWORT_NEW);
		
		Rechnungsadresse rechnungsadresse = new Rechnungsadresse();
		rechnungsadresse.setHausnummer(HAUSNUMMER_NEW);
		rechnungsadresse.setOrt(ORT_NEW);
		rechnungsadresse.setPlz(PLZ_NEW);
		rechnungsadresse.setStrasse(STRASSE_NEW);
		rechnungsadresse.setBenutzer(benutzer);
		
		benutzer.setRechnungsadresse(rechnungsadresse);
		
		thrown.expect(BenutzerDuplikatException.class);
		bv.createBenutzer(benutzer, LOCALE_DE, false);
	}
	
	@Test
	public void createBenutzerInValid() throws BenutzerValidationException, BenutzerDuplikatException {
		AbstractBenutzer benutzer = new AbstractBenutzer();
		benutzer.setEmail(EMAIL_NEW);
		benutzer.setNachname(NACHNAME_NEW);
		benutzer.setVorname(VORNAME_NEW);
		benutzer.setPasswort(PASSWORT_NEW);
		benutzer.setPasswortWdh("TEST");
		
		Rechnungsadresse rechnungsadresse = new Rechnungsadresse();
		rechnungsadresse.setHausnummer(HAUSNUMMER_NEW);
		rechnungsadresse.setOrt(ORT_NEW);
		rechnungsadresse.setPlz("ABC");
		rechnungsadresse.setStrasse(STRASSE_NEW);
		rechnungsadresse.setBenutzer(benutzer);
		
		benutzer.setRechnungsadresse(rechnungsadresse);
		
		thrown.expect(BenutzerValidationException.class);
		bv.createBenutzer(benutzer, LOCALE_DE, false);
	}
	
	@Test
	public void updateBenutzerUndAddLieferadresse() throws InvalidBenutzerIdException, NotFoundException, BenutzerValidationException, BenutzerDuplikatException {
		AbstractBenutzer benutzer = bv.findBenutzerByID(5L, LOCALE_DE);
		assertThat(benutzer.getLieferadresse(), is(nullValue()));
		
		benutzer.setKontonummer(KONTONUMMER_NEW);
		
		Lieferadresse lieferadresse = new Lieferadresse();
		lieferadresse.setHausnummer(HAUSNUMMER_NEW);
		lieferadresse.setOrt("Liefer");
		lieferadresse.setPlz(PLZ_NEW);
		lieferadresse.setStrasse("Lieferstr");
		
		lieferadresse.setName("Liefermann");
		lieferadresse.setVorname("Lars");
		
		benutzer.setLieferadresse(lieferadresse);
		lieferadresse.setBenutzer(benutzer);
		
		bv.updateBenutzer(benutzer, LOCALE_DE, false);
		
		benutzer = bv.findBenutzerByID(5L, LOCALE_DE);
		
		assertThat(benutzer.getKontonummer(), is(KONTONUMMER_NEW));
		assertThat(benutzer.getLieferadresse(), is(notNullValue()));
	}
	
	@Test
	public void createRolleHasBenutzer() throws RolleHasBenutzerValidationException, NotFoundException, InvalidBenutzerIdException, RolleHasBenutzerDuplikatException, InvalidRolleIdException {
		AbstractBenutzer benutzer = bv.findBenutzerByID(ID_EXISTS, LOCALE_DE);
		Rolle rolle = bv.findRolleById(2L, LOCALE_DE);

		bv.createRolleHasBenutzer(benutzer, rolle, LOCALE_DE, false);
		
		benutzer = bv.findBenutzerByIDFetchRollen(ID_EXISTS, LOCALE_DE);
		assertThat(benutzer.getRollehasbenutzer().size(), is(2));
	}
	
	@Test
	public void deleteRolleHasBenutzer() throws InvalidBenutzerIdException, NotFoundException, InvalidRolleException, InvalidRolleIdException {
		AbstractBenutzer benutzer = bv.findBenutzerByID(ID_EXISTS, LOCALE_DE);
		Rolle rolle = bv.findRolleById(2L, LOCALE_DE);
		/*RolleHasBenutzer rhb = null;
		for(RolleHasBenutzer tmp : benutzer.getRollehasbenutzer()) {
			if(tmp.getRolle().getRolle()==RolleTyp.MITARBEITER) {
				rhb = tmp;
				break;
			}
		}*/
		
		bv.deleteRolleHasBenutzer(benutzer, rolle);
		
		thrown.expect(NotFoundException.class);
		List<AbstractBenutzer> result = bv.findBenutzerNByRolle(RolleTyp.MITARBEITER, LOCALE_DE);
		
		result = bv.findBenutzerNByRolle(RolleTyp.ADMINISTRATOR, LOCALE_DE);
		assertThat(result.contains(benutzer), is(true));
		
		/*benutzer = bv.findBenutzerByIDFetchRollen(ID_EXISTS, LOCALE_DE);
		assertThat(benutzer.getRollehasbenutzer().size(), is(1));
		rhb.setRolle(bv.findRolleById(3L, LOCALE_DE));
		assertThat(benutzer.getRollehasbenutzer().contains(rhb), is(true));*/
	}
	
	@Test
	public void createProduktbewertung() throws InvalidBenutzerIdException, NotFoundException, InvalidArtikelIdException, ProduktbewertungValidationException, ProduktbewertungDuplikatException, ArtikelValidationException, ArtikelDuplikatException {
		AbstractBenutzer benutzer = bv.findBenutzerByID(ID_EXISTS, LOCALE_DE);
		Artikel artikel = new Artikel();
		artikel.setBezeichnung("Tshirt");
		artikel.setImsortiment(true);
		artikel.setPreis(19.99);
		artikel = av.createArtikel(artikel, LOCALE_DE, false);
		ARTIKEL_ID_NEW = artikel.getIdArtikel();
		//Artikel artikel = av.findArtikelById(ARTIKEL_ID_EXISTS, LOCALE_DE);
		
		Produktbewertung produktbewertung = new Produktbewertung();
		produktbewertung.setBewertung(PRODUKTBEWERTUNG_BEWERTUNG_NEW);
		produktbewertung.setKommentar(PRODUKTBEWERTUNG_KOMMENTAR_NEW);
		
		produktbewertung = bv.createProduktbewertung(produktbewertung, benutzer, artikel, LOCALE_DE, false);
		
		Produktbewertung produktbewertungNEW = bv.findProduktbewertungByBenutzerIdAndArtikelId(benutzer.getIdBenutzer(), artikel.getIdArtikel(), LOCALE_DE);
		
		assertThat(produktbewertungNEW, is(notNullValue()));
		assertThat(produktbewertungNEW.getArtikel().getIdArtikel(), is(artikel.getIdArtikel()));
		assertThat(produktbewertungNEW.getBenutzer().getIdBenutzer(), is(ID_EXISTS));
	}
	
	@Test
	public void createProduktbewertungDuplikat() throws InvalidBenutzerIdException, NotFoundException, InvalidArtikelIdException, ProduktbewertungValidationException, ProduktbewertungDuplikatException {
		AbstractBenutzer benutzer = bv.findBenutzerByID(ID_EXISTS, LOCALE_DE);
		Artikel artikel = av.findArtikelById(ARTIKEL_ID_EXISTS, LOCALE_DE);
		
		Produktbewertung produktbewertung = new Produktbewertung();
		produktbewertung.setBewertung(PRODUKTBEWERTUNG_BEWERTUNG_NEW);
		produktbewertung.setKommentar(PRODUKTBEWERTUNG_KOMMENTAR_NEW);
		
		thrown.expect(ProduktbewertungDuplikatException.class);
		produktbewertung = bv.createProduktbewertung(produktbewertung, benutzer, artikel, LOCALE_DE, false);
	}
	
	@Test
	public void deleteProduktbewertung() throws InterruptedException, InvalidBenutzerIdException, NotFoundException, InvalidProduktbewertungIdException {
		
		Long idPB = null;
		int countPBs = 0;
		AbstractBenutzer a = bv.findBenutzerByIDFetchProduktbewertungen(ID_EXISTS, LOCALE_DE);
		countPBs = a.getProduktbewertungen().size();
		for(Produktbewertung pb : a.getProduktbewertungen()) {
			if(pb.getArtikel().getIdArtikel().longValue() == ARTIKEL_ID_NEW.longValue()) {
				idPB = pb.getIdProduktbewertung();
				bv.deleteProduktbewertung(pb, LOCALE_DE);
			}
		}
		
		thrown.expect(NotFoundException.class);
		bv.findProduktbewertungById(idPB, LOCALE_DE);
		
		a = bv.findBenutzerByIDFetchProduktbewertungen(ID_EXISTS, LOCALE_DE);
		assertThat(a.getProduktbewertungen().size(), is(countPBs-1));
	}
	
	@Test
	public void deleteBenutzer() throws NotFoundException, InvalidBenutzerIdException {
		
		int countBenutzer = bv.findBenutzerN().size();
		
		AbstractBenutzer benutzer = bv.findBenutzerByID(ID_EXISTS, LOCALE_DE);
		
		bv.deleteBenutzer(benutzer);
		
		assertThat(bv.findBenutzerN().size(), is(countBenutzer-1));
	}
}
