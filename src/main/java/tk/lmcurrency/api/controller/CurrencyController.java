package tk.lmcurrency.api.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import net.bytebuddy.description.annotation.AnnotationValue.Sort;
import tk.lmcurrency.api.model.Currency;
import tk.lmcurrency.api.repository.CurrencyRepository;
import tk.lmcurrency.api.services.CurrencyServices;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/")
public class CurrencyController {

	@Autowired
	private CurrencyRepository currencyRepository;

	@GetMapping
	public List<Currency> allCurrency() {
		return currencyRepository.findAll();
	}

	@GetMapping(path = "/last")
	public Currency lastCurrency() {
		return currencyRepository.findLastCurrency();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<?> add(@RequestBody Currency currency) throws Exception {
		if (currency.getBrlValue() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("brlValue não pode ficar vazio!");
		}
		try {
			Date today = new Date();
			Currency tempCurrency = new Currency();
			tempCurrency.setThisDate(currency.getThisDate() == null ? today : currency.getThisDate());
			tempCurrency.setBrlValue(currency.getBrlValue());
			tempCurrency.setThisHour(currency.getThisHour() == null ? today.getHours() : currency.getThisHour());

			currencyRepository.save(tempCurrency);
			return ResponseEntity.ok(tempCurrency);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao inserir currency");
		}

	}

	private void saveCurrency(JsonNode rootNode) throws Exception {
		Currency tempCurrency = new Currency();
		Date currencyDate = new Date();
		if (Boolean.parseBoolean(rootNode.get("success").toString()) == true) {

			currencyDate.setTime(Integer.parseInt(rootNode.get("timestamp").toString().replace("\"", "")) * 1000L);

			tempCurrency.setThisDate(currencyDate);
			tempCurrency.setThisHour(currencyDate.getHours());
			tempCurrency.setBrlValue(Double.parseDouble(rootNode.get("rates").get("BRL").toString()));

			System.out.println(tempCurrency);
			currencyRepository.save(tempCurrency);
		}
	}

	@GetMapping(path = "/byDate")
	public ResponseEntity<?> byDate(@RequestParam(name = "startDate") String startDate,
			@RequestParam(name = "finalDate") String finalDate) throws Exception {

		Date pStartDate = new Date();
		Date pFinalDate = new Date();

		if (!startDate.matches("^(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /\\.](19|20)\\d\\d$")
				|| !finalDate.matches("^(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /\\.](19|20)\\d\\d$")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
					"Paramêtros de data estão inválidos ou não foram preenchidos. startDate e finalDate no formato DD/MM/YYYY");
		}

		String[] startDateSplited = startDate.split("/");
		String[] finalDateSplited = finalDate.split("/");

		pStartDate.setDate(Integer.parseInt(startDateSplited[0]));
		pFinalDate.setDate(Integer.parseInt(finalDateSplited[0]));
		pStartDate.setMonth(Integer.parseInt(startDateSplited[1]) - 1);
		pFinalDate.setMonth(Integer.parseInt(finalDateSplited[1]) - 1);
		pStartDate.setYear(Integer.parseInt(startDateSplited[2]) - 1900);
		pFinalDate.setYear(Integer.parseInt(finalDateSplited[2]) - 1900);

		if (pFinalDate.before(pStartDate) || pFinalDate.after(new Date()) || pStartDate.after(new Date())) {
			System.out.println("ERRO \n Data Inicial: " + pStartDate + " \n Data Final:" + pFinalDate
					+ " \n Data de Hoje:" + new Date());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERRO ao buscar /byDate, Datas inválidas!");
		}

		pStartDate.setHours(0);
		pFinalDate.setHours(23);

		System.out.println("Realizando busca de currencys de " + pStartDate.toLocaleString() + " até "
				+ pFinalDate.toLocaleString());

		List<Currency> currencyReturn = currencyRepository.findByThisDateBetween(pStartDate, pFinalDate);

		if (currencyReturn.size() == 0) {
			pStartDate.setMonth(Integer.parseInt(startDateSplited[1]));
			pFinalDate.setMonth(Integer.parseInt(finalDateSplited[1]));
			ArrayList<JsonNode> response = CurrencyServices.requestCurrencyToApi(pStartDate, pFinalDate);
			response.forEach(currency -> {
				try {
					saveCurrency(currency);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			pStartDate.setMonth(Integer.parseInt(startDateSplited[1]));
			pFinalDate.setMonth(Integer.parseInt(finalDateSplited[1]));
			currencyReturn = currencyRepository.findByThisDateBetween(pStartDate, pFinalDate);
			return ResponseEntity.ok(currencyReturn);
		}

		return ResponseEntity.ok(currencyReturn);
	}

	@GetMapping(path = "/today")
	public List<Currency> today() {
		Date today = new Date();
		Date startDate = new Date();
		startDate.setHours(0);
		startDate.setMinutes(0);
		startDate.setSeconds(0);
		System.out.println(today);
		System.out.println(startDate);
		return currencyRepository.findByThisDateBetween(startDate, today);
	}

	@GetMapping(path = "/lastWeek")
	public List<Currency> lastWeek() {
		return filterByDaysCurrency(7);
	}

	@GetMapping(path = "/lastMonth")
	public List<Currency> lastMonth() {
		return filterByDaysCurrency(30);
	}

	public List<Currency> filterByDaysCurrency(Integer days) {
		Date today = new Date();
		Date startDate = new Date(today.getTime() - days * 24 * 60 * 60 * 1000L);
		today.setMonth(today.getMonth());
		startDate.setMonth(startDate.getMonth());
		startDate.setHours(0);
		Date limitDateFinish = new Date();
		Date limitDateStart = new Date();
		limitDateStart.setTime(startDate.getTime());
		limitDateFinish.setTime(startDate.getTime() + 24 * 60 * 60 * 1000L);
		List<Currency> filteredCurrency = new ArrayList<Currency>();

		while (limitDateFinish.before(today)) {
			System.out.println(limitDateFinish);
			limitDateStart.setTime(limitDateFinish.getTime());
			limitDateFinish.setHours(23);
			limitDateStart.setHours(0);

//			List<Currency> queryCurrencys = currencyRepository.findByThisDateBetween(limitDateStart, limitDateFinish);
			List<Currency> queryCurrencys = reloadCurrencyByTime(limitDateStart,limitDateFinish);
			limitDateFinish.setTime(limitDateFinish.getTime() + 24 * 60 * 60 * 1000L);
			if (queryCurrencys.size() > 0) {
				filteredCurrency.add(queryCurrencys.get(queryCurrencys.size() - 1));
			} else {
				continue;
			}

		}

		return filteredCurrency;
	}

	@GetMapping(path = "/reloadCurrency")
	public void reloadAllCurrency() throws Exception {
		//Removido a busca para api externa por motivos de quota :( Porém os dados foram gerados automaticamente 
//		try {
//			currencyRepository.deleteAll();
//			Date startDate = new Date();
//			startDate.setTime(startDate.getTime() - 2 * 30 * 24 * 60 * 60 * 1000L);
//			Date finalDate = new Date();
//			ArrayList<JsonNode> response = CurrencyServices.requestCurrencyToApi(startDate, finalDate);
//			response.forEach(currency -> {
//				try {
//					saveCurrency(currency);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			});
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		Date startDate = new Date();
		startDate.setTime(startDate.getTime() - 12 * 30 * 24 * 60 * 60 * 1000L);
		Date finalDate = new Date();
//		currencyRepository.deleteAll();
		reloadCurrencyByTime(startDate,finalDate);
		
		
	}
	
	private List<Currency> reloadCurrencyByTime(Date startDate,Date finalDate) {
		System.out.println("[ReloadCurrencyByTime] Reloading Currencys...");
		List<Currency> allCurrencys = new ArrayList<Currency>();
		Date thisDate = new Date();		
		thisDate.setTime(startDate.getTime());
		while(thisDate.before(finalDate)) {
			Date tempStartDate = new Date();
			Date tempFinalDate = new Date();
			tempStartDate.setTime(thisDate.getTime());
			tempFinalDate.setTime(thisDate.getTime());
			
			tempStartDate.setHours(0);
			tempStartDate.setMinutes(0);
			tempStartDate.setSeconds(0);
			
			tempFinalDate.setHours(23);
			tempFinalDate.setMinutes(59);
			tempFinalDate.setSeconds(59);
			
			if(currencyRepository.findByThisDateBetween(tempStartDate, tempFinalDate).size() > 0) {
				thisDate.setTime(thisDate.getTime() + 60*60*1000L);
				continue;
			}else {
				Currency tempCurrency = new Currency();
				tempCurrency.setBrlValue( Math.random() * 1.5 + 5);
				tempCurrency.setThisDate(new Date(thisDate.getTime()));
				tempCurrency.setThisHour(thisDate.getHours());
				allCurrencys.add(tempCurrency);
				thisDate.setTime(thisDate.getTime() + 60*60*1000L);
			}
			
		}
		currencyRepository.saveAll(allCurrencys);
		System.out.println("[ReloadCurrencyByTime] Finished.");
		
		return allCurrencys.size() == 0 ? currencyRepository.findByThisDateBetween(startDate, finalDate) : allCurrencys;
	}


}
