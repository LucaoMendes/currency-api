package tk.lmcurrency.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/currency")
public class CurrencyController {
	
	@GetMapping
	public List<Currency> lastWeek() {
		return "Ultima Semana";
	}
}
