package tk.lmcurrency.api.controller;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import io.restassured.http.ContentType;
import tk.lmcurrency.api.model.Currency;
import tk.lmcurrency.api.repository.CurrencyRepository;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.*;
@WebMvcTest
public class CurrencyControllerTest {

	@Autowired
	private CurrencyController currencyController;
	
	@MockBean
	private CurrencyRepository currencyRepository;

	@BeforeEach
	public void setup() {
		System.out.println("[CurrencyController] Inicializando Setup de Testes.");
		standaloneSetup(this.currencyController);
	}

	@Test
	public void deveRetornarSucesso_QuandoBuscarCurrencysToday() {
		System.out.println("[CurrencyController] Testando /today.");
		Date today = new Date();
		Date startDate = new Date();
		startDate.setHours(0);
		
		when(this.currencyRepository.findByThisDateBetween(startDate, today)).thenReturn(new ArrayList<Currency>());
		
		given()
			.accept(ContentType.JSON)
			.when()
			.get("/today")
			.then()
			.statusCode(HttpStatus.OK.value());
	}
	
	@Test
	public void deveRetornarSucesso_QuandoBuscarCurrencysLastWeek() {
		System.out.println("[CurrencyController] Testando /lastWeek.");
		Date today = new Date();
		Date startDate = new Date();
		startDate.setHours(0);
		startDate.setTime(startDate.getTime() - 6 * 24 * 60 * 60 * 1000L);
		
		when(this.currencyRepository.findByThisDateBetween(startDate, today)).thenReturn(new ArrayList<Currency>());
		
		given()
			.accept(ContentType.JSON)
			.when()
			.get("/lastWeek")
			.then()
			.statusCode(HttpStatus.OK.value());
	}
	
	@Test
	public void deveRetornarSucesso_QuandoBuscarCurrencysLastMonth() {
		System.out.println("[CurrencyController] Testando /lastMonth.");
		Date today = new Date();
		Date startDate = new Date();
		startDate.setHours(0);
		startDate.setTime(startDate.getTime() - 30 * 24 * 60 * 60 * 1000L);
		
		when(this.currencyRepository.findByThisDateBetween(startDate, today)).thenReturn(new ArrayList<Currency>());
		
		given()
			.accept(ContentType.JSON)
			.when()
			.get("/lastWeek")
			.then()
			.statusCode(HttpStatus.OK.value());
	}
	
	@Test
	public void deveRetornarSucesso_QuandoBuscarCurrencysByDate() {
		System.out.println("[CurrencyController] Testando /byDate.");
		Date finalDate = new Date();
		Date startDate = new Date();
		startDate.setHours(0);
		startDate.setTime(startDate.getTime() - 7 * 24 * 60 * 60 * 1000L);
		
		String finalDateDay = finalDate.getDate() < 10 ? "0" + finalDate.getDate() : Integer.toString(finalDate.getDate());
		String startDateDay = startDate.getDate() < 10 ? "0" + startDate.getDate() : Integer.toString(startDate.getDate());
		
		String finalDateMonth = finalDate.getMonth() < 10 ? "0" + finalDate.getMonth() : Integer.toString(finalDate.getMonth());
		String startDateMonth = startDate.getMonth() < 10 ? "0" + startDate.getMonth() : Integer.toString(startDate.getMonth());
		
		String finalDateString = finalDateDay +"/"+ finalDateMonth +"/"+ (finalDate.getYear() + 1900);
		String startDateString = startDateDay +"/"+ startDateMonth +"/"+ (startDate.getYear() + 1900);
		
		System.out.println("[CurrencyController] Passando datas: "+ startDateString + " e " + finalDateString);
		
		when(this.currencyRepository.findByThisDateBetween(startDate, finalDate))
			.thenReturn(new ArrayList<Currency>());
		
		given()
			.accept(ContentType.JSON)
			.when()
			.get("/byDate?startDate={startDate}&finalDate={finalDate}",startDateString,finalDateString)
			.then()
			.statusCode(HttpStatus.OK.value());
	}
	
	@Test
	public void deveRetornarFalha_QuandoBuscarCurrencysByDate() {
		System.out.println("[CurrencyController] Testando /byDate.");
		Date finalDate = new Date();
		Date startDate = new Date();
		startDate.setHours(0);
		startDate.setTime(startDate.getTime() - 7 * 24 * 60 * 60 * 1000L);
		
		String finalDateDay = "00" + finalDate.getDate();
		String startDateDay = "ab";
		
		String finalDateMonth = finalDate.getMonth() < 10 ? "0" + finalDate.getMonth() : Integer.toString(finalDate.getMonth());
		String startDateMonth = startDate.getMonth() < 10 ? "0" + startDate.getMonth() : Integer.toString(startDate.getMonth());
		
		String finalDateString = finalDateDay +"/"+ finalDateMonth +"/"+ (finalDate.getYear() + 1900);
		String startDateString = startDateDay +"/"+ startDateMonth +"/"+ (startDate.getYear() + 1900);
		
		System.out.println("[CurrencyController] Passando datas: "+ startDateString + " e " + finalDateString);
		
		when(this.currencyRepository.findByThisDateBetween(startDate, finalDate))
			.thenReturn(new ArrayList<Currency>());
		
		given()
			.accept(ContentType.JSON)
			.when()
			.get("/byDate?startDate={startDate}&finalDate={finalDate}",startDateString,finalDateString)
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value());
		
		
		given()
		.accept(ContentType.JSON)
		.when()
		.get("/byDate?startDate={startDate}&finalDate={finalDate}","06/08/2022","05/08/2022")
		.then()
		.statusCode(HttpStatus.BAD_REQUEST.value());
		
		finalDate.setTime(finalDate.getTime() + 1 * 24 * 60 * 60 * 1000L);
		finalDateString = finalDateDay +"/"+ finalDateMonth +"/"+ (finalDate.getYear() + 1900);
		
		given()
		.accept(ContentType.JSON)
		.when()
		.get("/byDate?startDate={startDate}&finalDate={finalDate}","06/08/2022",finalDateString)
		.then()
		.statusCode(HttpStatus.BAD_REQUEST.value());
	}
	
	
}
