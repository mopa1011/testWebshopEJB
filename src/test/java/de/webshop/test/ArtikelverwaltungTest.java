package de.webshop.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.ejb.EJB;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.ejb3.annotation.IgnoreDependency;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.webshop.artikelverwaltung.domain.Artikel;
import de.webshop.artikelverwaltung.domain.Kategorie;
import de.webshop.artikelverwaltung.domain.KategorieHasArtikel;
import de.webshop.artikelverwaltung.domain.Produktbewertung;
import de.webshop.artikelverwaltung.service.ArtikelDuplikatException;
import de.webshop.artikelverwaltung.service.ArtikelValidationException;
import de.webshop.artikelverwaltung.service.Artikelverwaltung;
import de.webshop.artikelverwaltung.service.ArtikelverwaltungDao;
import de.webshop.artikelverwaltung.service.InvalidArtikelIdException;
import de.webshop.artikelverwaltung.service.InvalidKategorieBezeichnungException;
import de.webshop.artikelverwaltung.service.InvalidKategorieIdException;
import de.webshop.artikelverwaltung.service.KategorieHasArtikelDuplikatException;
import de.webshop.artikelverwaltung.service.KategorieHasArtikelValidationException;
import de.webshop.util.NotFoundException;

@RunWith(Arquillian.class)
public class ArtikelverwaltungTest extends AbstractTest {
	
	private static final Long IDARTIKEL_VORHANDEN = Long.valueOf(1);
	private static final Long IDARTIKEL_NICHTVORHANDEN = 55555L;
	private static final Long IDARTIKEL_INVALID = -1L;
	
	private static final String BEZEICHNUNG_VORHANDEN = "hose";
	private static final String BEZEICHNUNG_NICHTVORHANDEN = "Schottenrock";
	private static final Long KATEGORIE_VORHANDEN = Long.valueOf(1);
	private static final String KATEGORIE_BEZEICHNUNG_VORHANDEN = "Jeans";
	
	private static final String NEU_BEZEICHNUNG = "Strickjacke";
	private static final Double NEU_PREIS = 15.60;
	private static final Double NEU_PREIS_INVALID = -15.77;
	
	
	@EJB
	private Artikelverwaltung av;
			
	@Test
	public void findArtikelById() throws  NotFoundException, InvalidArtikelIdException {
		
		final Long idartikel = IDARTIKEL_VORHANDEN;
		
		Artikel artikel = av.findArtikelById(idartikel, LOCALE_DE);
		
		assertThat(idartikel, is(artikel.getIdArtikel()));
	}
	
	@Test
	public void findArtikelByIdNichtVorhanden() throws InvalidArtikelIdException, NotFoundException {
		thrown.expect(NotFoundException.class);
		av.findArtikelById(IDARTIKEL_NICHTVORHANDEN, LOCALE_DE);
	}
	
	@Test
	public void findArtikelByIdInvalid() throws InvalidArtikelIdException, NotFoundException {
		thrown.expect(InvalidArtikelIdException.class);
		av.findArtikelById(IDARTIKEL_INVALID, LOCALE_DE);
	}
	
	@Test
	public void findArtikelByBezeichnung() throws NotFoundException {
		
		final String bezeichnung = BEZEICHNUNG_VORHANDEN;
		
		List<Artikel> artikel = av.findArtikelByBezeichnung(bezeichnung);
		
		assertThat(artikel.isEmpty(), is(false));
		for(Artikel a : artikel) {
			assertThat(a.getBezeichnung(), is(bezeichnung));
		}
	}
	
	@Test 
	public void findArtikelByBezeichnungNichtVorhanden() throws NotFoundException {
		thrown.expect(NotFoundException.class);
		av.findArtikelByBezeichnung(BEZEICHNUNG_NICHTVORHANDEN);
	}
	
	
	/*TODO: Test macht so keinen Sinn bzw. die Methode findArtikelByKategorieId(),
	 * 		weil intern die NamedQuery FIND_ARTIKEL_BY_KATEGORIE genutz wird und diese eine Kategoriebezeichnung und keine ID erwartet!
	 * 
	 */
	@Test
	public void findArtikelByKategorieId() throws NotFoundException, InvalidKategorieIdException {
		
		Kategorie kategorie = av.findKategorieById(KATEGORIE_VORHANDEN, LOCALE_DE);
		Boolean foundkategorie = false;
		List<Artikel> artikel = av.findArtikelByKategorieId(KATEGORIE_VORHANDEN, LOCALE_DE);
		
		assertThat(artikel.isEmpty(), is(false));
		
		for(Artikel a : artikel) {
			for(KategorieHasArtikel k : a.getKategorien()) {
				if(k.getKategorie().equals(kategorie))
					foundkategorie = true;
			}
			assertThat(foundkategorie, is(true));
			foundkategorie = false;
		}		
	}
	
	@Test
	public void findArtikelByKategorieBez() throws NotFoundException, InvalidKategorieBezeichnungException {
		final String kategorie = KATEGORIE_BEZEICHNUNG_VORHANDEN;
		Boolean foundkategorie = false;
		
		List<Artikel> artikel = av.findArtikelByKategorie(kategorie, LOCALE_DE);
		for(Artikel a : artikel) {
			for(KategorieHasArtikel k : a.getKategorien()) {
				if(k.getKategorie().getBezeichnung().equals(KATEGORIE_BEZEICHNUNG_VORHANDEN)) {
					foundkategorie = true;
				}
			assertThat(foundkategorie, is(true));
			foundkategorie = false;
			}
		}	
	}
	
	@Test
	public void findProduktbewertungByArtikelId() throws NotFoundException, InvalidArtikelIdException {
		
		final Long idartikel = IDARTIKEL_VORHANDEN;
		
		List<Produktbewertung> bewertung = av.findProduktbewertungByArtikelId(idartikel, LOCALE_DE);
		
		assertThat(bewertung.isEmpty(), is(false));
		for(Produktbewertung b : bewertung)
			assertThat(b.getArtikel().getIdArtikel(), is(idartikel));	
		
	}
	
	@Test
	public void createKategorieHasArtikel() throws InvalidArtikelIdException, NotFoundException, InvalidKategorieIdException, KategorieHasArtikelValidationException, KategorieHasArtikelDuplikatException {
		Artikel artikel = av.findArtikelById(IDARTIKEL_VORHANDEN, LOCALE_DE);
		Kategorie kategorie = av.findKategorieById(KATEGORIE_VORHANDEN, LOCALE_DE);
		
		av.createKategorieHasArtikel(kategorie, artikel, LOCALE_DE, false);
	
		List<Artikel> artikellist = av.findArtikelByKategorieId(KATEGORIE_VORHANDEN, LOCALE_DE);
		
		assertThat(artikellist.isEmpty(), is(false));
		assertThat(artikellist.contains(artikel), is(true));
		
	}
	
	@Test
	public void createArtikel() throws ArtikelValidationException, ArtikelDuplikatException, NotFoundException {
		
		final String bezeichnung = NEU_BEZEICHNUNG;
		final Double preis = NEU_PREIS;
		
		Artikel artikel = new Artikel();
		
		artikel.setBezeichnung(bezeichnung);
		artikel.setImsortiment(false);
		artikel.setPreis(preis);
		artikel.setIdArtikel(null);
		
		
		av.createArtikel(artikel, LOCALE_DE, false);
	}
	
	@Test
	public void createArtikelInvalid() throws ArtikelValidationException, ArtikelDuplikatException, NotFoundException {
		Artikel artikel = new Artikel();
		artikel.setBezeichnung("Schottenrock");
		artikel.setImsortiment(true);
		artikel.setPreis(NEU_PREIS_INVALID);
		artikel.setIdArtikel(null);
		
		thrown.expect(ArtikelValidationException.class);
		av.createArtikel(artikel, LOCALE_DE, false);
	}
	
	@Test
	public void updateArtikel() throws InvalidArtikelIdException, NotFoundException, ArtikelValidationException {
		Artikel artikel = av.findArtikelById(IDARTIKEL_VORHANDEN, LOCALE_DE);
		
		artikel.setPreis(NEU_PREIS);
		
		av.updateArtikel(artikel, LOCALE_DE, false);
		
		artikel = av.findArtikelById(IDARTIKEL_VORHANDEN, LOCALE_DE);
		
		assertThat(artikel.getPreis(),is(NEU_PREIS));
	}
	
	@Test
	public void updateArtikelInvalid() throws InvalidArtikelIdException, NotFoundException, ArtikelValidationException {
		Artikel artikel = av.findArtikelById(IDARTIKEL_VORHANDEN, LOCALE_DE);
		
		artikel.setPreis(NEU_PREIS_INVALID);
		thrown.expect(ArtikelValidationException.class);
		av.updateArtikel(artikel, LOCALE_DE, false);
	}
	
}

