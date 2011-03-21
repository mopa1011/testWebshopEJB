package de.webshop.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.ejb.EJB;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.webshop.artikelverwaltung.domain.Artikel;
import de.webshop.artikelverwaltung.service.ArtikelValidationException;
import de.webshop.artikelverwaltung.service.Artikelverwaltung;
import de.webshop.artikelverwaltung.service.InvalidArtikelIdException;
import de.webshop.bestellungsverwaltung.service.InvalidBestellungIdException;
import de.webshop.lagerverwaltung.domain.Lager;
import de.webshop.lagerverwaltung.domain.Lager.Farbe;
import de.webshop.lagerverwaltung.service.InvalidLagerIdException;
import de.webshop.lagerverwaltung.service.LagerArtikelDuplikatException;
import de.webshop.lagerverwaltung.service.LagerartikelValidationException;
import de.webshop.lagerverwaltung.service.Lagerverwaltung;
import de.webshop.util.NotFoundException;

@RunWith(Arquillian.class)
public class LagerverwaltungTest extends AbstractTest {

	private static final Long LAGER_ID_VORHANDEN = 1L;
	private static final Long ARTIKEL_ID_VORHANDEN = 1L;
	private static final String GROESSE = "XXL";
	private static final Integer MINBESTAND = 10;
	private static final Integer ISTBESTAND = 1337;
	
	@EJB
	private Lagerverwaltung lv;
	
	@EJB
	private Artikelverwaltung av;
	//TODO Test funktionieren beide noch nicht NullPointerException wer will kann es loesen !!
	//@Ignore
	@Test
	public void findLagerById() throws InvalidBestellungIdException, NotFoundException, InvalidLagerIdException {
		final Long lagerId = LAGER_ID_VORHANDEN;
		Lager lager = lv.findLagerById(lagerId, LOCALE_DE);
		
		assertThat(lagerId, is(lager.getIdlager()));
	}
	
	@Test
	public void findLagerByArtikelId() throws NotFoundException, InvalidArtikelIdException{
		final Long idArtikel = ARTIKEL_ID_VORHANDEN;
		
		List<Lager> lagerN = lv.findLagerByArtikelId(ARTIKEL_ID_VORHANDEN, LOCALE_DE);
		
		assertThat(lagerN.isEmpty(), is(false));
		for (Lager l : lagerN) 
		{
			assertThat(idArtikel, is(l.getArtikel().getIdArtikel()));
		}
	}
	
	@Test
	public void createLagerartikel() throws InvalidArtikelIdException, NotFoundException, LagerArtikelDuplikatException, LagerartikelValidationException, ArtikelValidationException {
		
		Artikel artikel = av.findArtikelById(ARTIKEL_ID_VORHANDEN, LOCALE_DE);
		
		Lager lagerArtikel = new Lager();
		lagerArtikel.setGroesse(GROESSE);
		lagerArtikel.setBestandMin(MINBESTAND);
		lagerArtikel.setBestandIst(ISTBESTAND);
		lagerArtikel.setFarbe(Farbe.ROT);
		lagerArtikel.setIdlager(null);
		
		lagerArtikel = lv.createLagerArtikel(lagerArtikel, artikel, LOCALE_DE, false);
		
		assertThat(lagerArtikel.getBestandIst(), is(ISTBESTAND));
	}
	
	
	// funktioniert nur bei Lagerartikel, welche noch nicht in einer Bestellung verwendet wurden
	@Ignore
	@Test 
	public void deleteLagerartikel() throws InvalidLagerIdException, NotFoundException, InvalidArtikelIdException, ArtikelValidationException {
		Lager lagerartikel = lv.findLagerById(LAGER_ID_VORHANDEN, LOCALE_DE);
		Long artikelid = lagerartikel.getArtikel().getIdArtikel();
		
		lv.deleteLagerartikel(lagerartikel, LOCALE_DE);
		Artikel artikel = av.findArtikelById(artikelid, LOCALE_DE);
		
		assertFalse(artikel.getArtikelVarianten().contains(lagerartikel));
		
		
		thrown.expect(NotFoundException.class);
		lv.findLagerById(LAGER_ID_VORHANDEN, LOCALE_DE);
		
	}
	
}
