package de.webshop.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import de.webshop.artikelverwaltung.service.Artikelverwaltung;
import de.webshop.artikelverwaltung.service.InvalidArtikelIdException;
import de.webshop.benutzerverwaltung.domain.AbstractBenutzer;
import de.webshop.benutzerverwaltung.service.Benutzerverwaltung;
import de.webshop.benutzerverwaltung.service.InvalidBenutzerIdException;
import de.webshop.bestellungsverwaltung.domain.Bestellposition;
import de.webshop.bestellungsverwaltung.domain.Bestellung;
import de.webshop.bestellungsverwaltung.domain.Bestellung.Bestellstatus;
import de.webshop.bestellungsverwaltung.domain.Bestellung.Zahlungsart;
import de.webshop.bestellungsverwaltung.service.BestellungDeleteException;
import de.webshop.bestellungsverwaltung.service.BestellungDuplikatException;
import de.webshop.bestellungsverwaltung.service.BestellungValidationException;
import de.webshop.bestellungsverwaltung.service.Bestellverwaltung;
import de.webshop.bestellungsverwaltung.service.InvalidBestellungIdException;
import de.webshop.lagerverwaltung.domain.Lager;
import de.webshop.lagerverwaltung.service.InvalidLagerIdException;
import de.webshop.lagerverwaltung.service.LagerartikelValidationException;
import de.webshop.lagerverwaltung.service.Lagerverwaltung;
import de.webshop.util.NotFoundException;


@RunWith(Arquillian.class)
public class BestellverwaltungTest extends AbstractTest {
	
	private static final Long BESTELL_ID_VORHANDEN = 2L;
	private static final Long BESTELL_ID_NICHT_VORHANDEN = 199L;
	private static final Long BESTELL_ID_FALSCH = -10L;
	
	private static final Long BESTELL_ID_VORHANDEN2 = 1L;
	
	private static final Long BESTELL_ID_VORHANDEN3 = 3L;
	
	private static final Bestellstatus STATUS_VORHANDEN =  Bestellstatus.BESTELLT;  
	private static final Bestellstatus STATUS_VORHANDEN2 = Bestellstatus.VERSENDET; 
	private static final Bestellstatus STATUS_VORHANDEN3 = Bestellstatus.WARENKORB; 
	
	private static final Zahlungsart ZAHLUNGSART_VORHANDEN = Zahlungsart.RECHNUNG;
	
	private static final Long BENUTZER_ID_VORHANDEN = 1L;
	private static final Long BENUTZER_ID_NICHT_VORHANDEN = Long.valueOf(99);
	
	private static final Long LAGER_ID_VORHANDEN1 = Long.valueOf(1);
	private static final Long LAGER_ID_VORHANDEN2 = Long.valueOf(2);
	
	private static final Long ARTIKEL_1_ID = Long.valueOf(1);
	private static final Long LAGER_1_ANZAHL = Long.valueOf(4);
	private static final Long ARTIKEL_2_ID = Long.valueOf(2);
	private static final Long LAGER_2_ANZAHL = Long.valueOf(5);
	
	private static final Long BP_ID_VORHANDEN = 2L;
	
	private Integer bestandVorher = 0;
	private Integer bestandNachher =  0;
	
	@EJB
	private Bestellverwaltung bv;
	@EJB
	private Benutzerverwaltung benutzerverwaltung;
	@EJB
	private Artikelverwaltung av;
	@EJB
	private Lagerverwaltung lv;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void findBestellungByIdVorhanden()throws InvalidBestellungIdException, NotFoundException{
		final Long bestellId = BESTELL_ID_VORHANDEN;
		
		Bestellung bestellung = bv.findBestellungById(bestellId, LOCALE_DE);

		assertThat(bestellId, is(bestellung.getIdbestellung()));
	}
	
	@Test
	public void findBestellpositionByIdVorhanden()throws NotFoundException{
		final Long bpId = BP_ID_VORHANDEN;
		
		Bestellposition bp = bv.findBestellpositionById(bpId);

		assertThat(bpId, is(bp.getIdbestellposition()));
	}
	
	@Test
	public void findBestellungByIdNichtVorhanden()throws InvalidBestellungIdException, NotFoundException{
		final Long bestellId = BESTELL_ID_NICHT_VORHANDEN;
		
		thrown.expect(NotFoundException.class);
		thrown.expectMessage("" + bestellId);
		
		Bestellung bestellung = bv.findBestellungById(bestellId, LOCALE_DE);
		
		assertThat (bestellung, is(nullValue()));
	}
	
	@Test
	public void findBestellungByIdFalsch()throws InvalidBestellungIdException, NotFoundException{
		final Long bestellId = BESTELL_ID_FALSCH;
		
		thrown.expect(InvalidBestellungIdException.class);
		thrown.expectMessage("" + bestellId);
		
		Bestellung bestellung = bv.findBestellungById(bestellId, LOCALE_DE);
		
		assertThat (bestellung, is(nullValue()));
	}
	
	
	@Test
	public void findBestellungByStatus() throws NotFoundException{
		final Bestellstatus status = STATUS_VORHANDEN;
		
		List<Bestellung> bestellungen = bv.findBestellungByBestellstatus(status, LOCALE_DE);
		
		assertThat(bestellungen.isEmpty(), is(false));
		for (Bestellung b : bestellungen) 
		{
			assertThat(b.getBestellstatus(), is(status));
		}
	}
	
	
	@Test
	public void findBestellungByBenutzerVorhanden() throws NotFoundException, InvalidBenutzerIdException{
		final Long benutzerId =  BENUTZER_ID_VORHANDEN;
		
		AbstractBenutzer benutzer = benutzerverwaltung.findBenutzerByID(BENUTZER_ID_VORHANDEN, LOCALE_DE);
		
		List<Bestellung> bestellungen = bv.findBestellungenByBenutzerId(benutzer.getIdBenutzer(), LOCALE_DE);
		
		assertThat(bestellungen.isEmpty(), is(false));
		for (Bestellung b : bestellungen) 
		{
			assertThat(b.getBenutzer().getIdBenutzer(), is(benutzerId));
		}
	}
	
	@Test
	public void findBestellungByByBenutzerNichtVorhanden()throws NotFoundException, InvalidBenutzerIdException{
		final Long benutzerId = BENUTZER_ID_NICHT_VORHANDEN;
		
		thrown.expect(NotFoundException.class);
		thrown.expectMessage("" + benutzerId);
		
		AbstractBenutzer benutzer = benutzerverwaltung.findBenutzerByID(BENUTZER_ID_NICHT_VORHANDEN, LOCALE_DE);
		
		List<Bestellung> bestellungen = bv.findBestellungenByBenutzerId(benutzer.getIdBenutzer(), LOCALE_DE);
		
		assertThat(bestellungen.isEmpty(), is(true));
	}
	//TODO muss nicht jede bestellposition auch einzelnd created werden?
	
	@Test
	public void createBestellung() throws BestellungValidationException, BestellungDuplikatException, NotFoundException,InvalidArtikelIdException, InvalidBenutzerIdException, InvalidLagerIdException{
		final Long benutzerId = BENUTZER_ID_VORHANDEN;
		
		final Long idLager1 = LAGER_ID_VORHANDEN1;
		final Long idLager2 = LAGER_ID_VORHANDEN2;
		
		final Bestellstatus bestellstatus = STATUS_VORHANDEN3;
		final Zahlungsart zahlungsart = ZAHLUNGSART_VORHANDEN;
		
		AbstractBenutzer benutzer = benutzerverwaltung.findBenutzerByID(BENUTZER_ID_VORHANDEN, LOCALE_DE);
		
		assertThat(BENUTZER_ID_VORHANDEN, is(benutzer.getIdBenutzer()));
		
		Lager lager = lv.findLagerById(idLager1, LOCALE_DE);
		
		assertThat(LAGER_ID_VORHANDEN1, is(lager.getIdlager()));
		
		Bestellposition eins = new Bestellposition(); 
		eins.setMenge(new Integer(2));
		eins.setLagerArtikel(lager);
		
		Bestellung newBestellung = new Bestellung();
		
		newBestellung.addBestellposition(eins);
		newBestellung.setBenutzer(benutzer);
		newBestellung.setBestellstatus(bestellstatus);
		newBestellung.setZahlungsart(zahlungsart);
		bv.createBestellung(newBestellung, benutzer, LOCALE_DE, false);
	}
	
	@Test
	public void updateBestellungStatus()throws BestellungValidationException, BestellungDuplikatException, NotFoundException, InvalidBestellungIdException, LagerartikelValidationException{
		final Long bestellId = BESTELL_ID_VORHANDEN;
		final Bestellstatus status = STATUS_VORHANDEN2;
		//500 ist richtig
		final Double gesamtPreis = Double.valueOf(500.00);
		
		Bestellung bestellung = bv.findBestellungById(bestellId, LOCALE_DE);
		
		bestellung.setBestellstatus(status);
		
		bv.updateBestellung(bestellung,LOCALE_DE, false);
		
		assertThat(status, is(bestellung.getBestellstatus()));
		assertThat(gesamtPreis, is(bestellung.getGesamtpreis()));
		
	}
	
	@Test
	public void deleteBestellungStatusaenderung() throws NotFoundException, BestellungValidationException, BestellungDeleteException, InvalidBestellungIdException, LagerartikelValidationException{
		final Long bestellId = BESTELL_ID_VORHANDEN;
		
		Bestellung bestellung = bv.findBestellungById(bestellId, LOCALE_DE);
		
		assertThat(bestellId, is(bestellung.getIdbestellung()));
		
		bv.deleteBestellung(bestellung, LOCALE_DE, false);
		
		bestellung = bv.findBestellungById(bestellId, LOCALE_DE);
		
		assertThat (bestellung.getBestellstatus(), is(Bestellstatus.STORNIERT));
	}
	
	@Test
	public void deleteBestellpositionen() throws NotFoundException, BestellungValidationException, BestellungDeleteException, InvalidBestellungIdException, LagerartikelValidationException{ 
		final Long bestellId = BESTELL_ID_VORHANDEN2;
		
		Bestellung bestellung = bv.findBestellungById(bestellId, LOCALE_DE);
		
		assertThat(bestellId, is(bestellung.getIdbestellung()));
		
		for (Bestellposition bp : bestellung.getBestellpositionen()) {
			bestandVorher = bp.getLagerArtikel().getBestandIst();
			
			bv.deleteBestellposition(bp, LOCALE_DE, false);
			
			bestandNachher = bp.getLagerArtikel().getBestandIst();
			assertThat(bestandNachher.equals(bestandVorher), is(false));
		}
		bestellung = bv.findBestellungById(bestellId, LOCALE_DE);
		assertThat(bestellId, is(bestellung.getIdbestellung()));
		assertThat(bestellung.getBestellpositionen().isEmpty(), is(true));
	}
	
	@Test
	public void deleteBestellungKomplettLoeschen() throws NotFoundException, BestellungValidationException, BestellungDeleteException, InvalidBestellungIdException, LagerartikelValidationException{
		final Long bestellId = BESTELL_ID_VORHANDEN3;
		final Long bpId = BP_ID_VORHANDEN;
		
		Bestellung bestellung = bv.findBestellungById(bestellId, LOCALE_DE);
		
		assertThat(bestellId, is(bestellung.getIdbestellung()));
		
		List<Integer> bestandVorher = new ArrayList<Integer>();
		for (Bestellposition bp : bestellung.getBestellpositionen()) {
			bestandVorher.add(bp.getLagerArtikel().getBestandIst());
		}
		
		bv.deleteBestellung(bestellung, LOCALE_DE, false);
		
		int i = 0;
		for (Bestellposition bp : bestellung.getBestellpositionen()) {
			assertThat(bestandVorher.get(i).equals(bp.getLagerArtikel().getBestandIst()),is(false));
			i++;
		}
		
		thrown.expect(NotFoundException.class);
		thrown.expectMessage("" + bestellId);
		
		bestellung = bv.findBestellungById(bestellId, LOCALE_DE);
		
		assertThat (bestellung, is(nullValue()));
		
		thrown.expect(NotFoundException.class);
		thrown.expectMessage("" + bpId);
		
		Bestellposition bp = bv.findBestellpositionById(bpId);
		assertThat (bp, is(nullValue()));
	}
	
	@Test
	public void updateBestellungBestellpositionHinzu()throws BestellungValidationException, BestellungDuplikatException, NotFoundException, InvalidBestellungIdException, InvalidLagerIdException, LagerartikelValidationException{
		final Long bestellId = BESTELL_ID_VORHANDEN;
		final Long idLager1 = LAGER_ID_VORHANDEN1;

		final Double gesamtPreis = Double.valueOf(550.00);
		Lager lager = lv.findLagerById(idLager1,LOCALE_DE);
		
		Bestellung bestellung = bv.findBestellungById(bestellId, LOCALE_DE);
		
		final int countBP = bestellung.getBestellpositionen().size();
		
		Bestellposition bp = new Bestellposition();
		bp.setLagerArtikel(lager);
		bp.setMenge(1);
		
		bestellung.addBestellposition(bp);
		
		bestellung = bv.updateBestellung(bestellung,LOCALE_DE, false);

		//bp = bv.findBestellpositionById(idBpNeu);
		
		assertThat(countBP+1, is(bestellung.getBestellpositionen().size()));
		assertThat(gesamtPreis, is(bestellung.getGesamtpreis()));
		
	}
	
	
}
