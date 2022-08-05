package tk.lmcurrency.api.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import tk.lmcurrency.api.model.Currency;
import tk.lmcurrency.api.repository.CurrencyRepository;

@RestController
@RequestMapping("/currency")
public class CurrencyController {
	
	@Autowired
	private CurrencyRepository currencyRepository;
	
	@GetMapping
	public List<Currency> allCurrency() {
		return currencyRepository.findAll();
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Currency add(@RequestBody Currency currency)  throws Exception{
		try {
	        Date today = new Date();
			
			Currency tempCurrency = new Currency();
			tempCurrency.setThisDate(currency.getThisDate() == null ? today : currency.getThisDate());
			tempCurrency.setThisHour(currency.getThisHour() == null ? new Date().getHours() : currency.getThisHour());
			tempCurrency.setBrlValue(currency.getBrlValue() == null ? currency.getBrlValue() : currency.getBrlValue());
			
			
			return currencyRepository.save(tempCurrency);
		}catch( Exception e ) {
			
		}
		return null;
	}
	
	@GetMapping(path = "/lastHours")
	public List<Currency> lastHours() {
		Date today = new Date();
		Date finalDate = new Date(today.getTime() - 7 * 60 * 60 * 1000L);
		
		System.out.println(finalDate + "ENTRE" + today);
		
		return currencyRepository.findByThisDateBetween(finalDate, today);
	}
	
	@GetMapping(path = "/lastWeek")
	public List<Currency> lastWeek() {
		Date today = new Date();
		Date startDate = new Date(today.getTime() - 6 * 24 * 60 * 60 * 1000L);
		startDate.setHours(0);
		Date limitDateFinish = new Date();
		Date limitDateStart = new Date();
		List<Currency> filteredCurrency = new ArrayList<Currency>();
		
		while(limitDateFinish.after(startDate)){
			limitDateStart.setTime(limitDateFinish.getTime());
			
			limitDateFinish.setHours(23);
			limitDateStart.setHours(0);
			List<Currency> queryCurrencys = currencyRepository.findByThisDateBetween(limitDateStart, limitDateFinish);
			if(queryCurrencys.size() > 0) {
				filteredCurrency.add(queryCurrencys.get(0));
				limitDateFinish.setTime(limitDateFinish.getTime() - 24 * 60 * 60 * 1000L);
			}else {
				break;
			}
		}
		
		return filteredCurrency;
	}
	
	@GetMapping(path = "/lastMonth")
	public List<Currency> lastMonth() {
		Date today = new Date();
		Date startDate = new Date(today.getTime() - 7 * 30 * 24 * 60 * 60 * 1000L);
		startDate.setHours(0);
		Date limitDateFinish = new Date();
		Date limitDateStart = new Date();
		List<Currency> filteredCurrency = new ArrayList<Currency>();
		
		while(limitDateFinish.after(startDate)){
			limitDateStart.setTime(limitDateFinish.getTime());
			
			limitDateFinish.setHours(23);
			limitDateStart.setHours(0);
			List<Currency> queryCurrencys = currencyRepository.findByThisDateBetween(limitDateStart, limitDateFinish);
			if(queryCurrencys.size() > 0) {
				filteredCurrency.add(queryCurrencys.get(0));
				limitDateFinish.setTime(limitDateFinish.getTime() - 24 * 60 * 60 * 1000L);
			}else {
				break;
			}
		}
		
		return filteredCurrency;
	}
	
	@GetMapping(path = "/reloadCurrency")
	public List<Currency> reloadAllCurrency(){
		Integer totalReq = 0;
		
		Date today = new Date();
		Date startDate = new Date(today.getTime() - 30 * 24 * 60 *60 *1000L );
		Date tempDate = new Date(startDate.getTime());
		currencyRepository.deleteAll();
		while(tempDate.before(today)) {
			Currency tempCurrency = new Currency();
			Integer hours = (int) Math.random() * 3 + 22;
			tempDate.setTime(tempDate.getTime() + 1 * 60 *60 *1000L);
			System.out.println(tempDate);
			tempCurrency.setBrlValue(Math.random() * 6);
			tempCurrency.setThisDate(new Date(tempDate.getTime()));
			tempCurrency.setThisHour(tempDate.getHours());
			currencyRepository.save(tempCurrency);
		}
		
		
		return currencyRepository.findAll();
	}
	
}
