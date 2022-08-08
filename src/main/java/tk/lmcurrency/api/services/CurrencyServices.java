package tk.lmcurrency.api.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore.Entry;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import tk.lmcurrency.api.controller.CurrencyController;
import tk.lmcurrency.api.model.Currency;
import tk.lmcurrency.api.repository.CurrencyRepository;

public class CurrencyServices {

	public static ArrayList<JsonNode> requestCurrencyToApi(Date startDate, Date finalDate) throws Exception {
		ArrayList<JsonNode> allNodes = new ArrayList<JsonNode>();
		try {
			if (startDate == null && finalDate == null) {
				HttpRequest request = HttpRequest.newBuilder()
						.uri(URI.create("https://fixer-fixer-currency-v1.p.rapidapi.com/latest?base=USD&symbols=BRL"))
						.header("X-RapidAPI-Key", "a9b6e16bbemshf3fbf9209955845p1ab613jsn2cdaf96d18f7")
						.header("X-RapidAPI-Host", "fixer-fixer-currency-v1.p.rapidapi.com")
						.method("GET", HttpRequest.BodyPublishers.noBody()).build();
				HttpResponse<String> response = HttpClient.newHttpClient().send(request,
						HttpResponse.BodyHandlers.ofString());
				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(response.body());
				allNodes.add(rootNode);
			} else {
				Date tempDate = startDate;

				while (tempDate.before(finalDate)) {
					String day = tempDate.getDate() < 10 ? "0" + tempDate.getDate()
							: Integer.toString(tempDate.getDate());
					String month = tempDate.getMonth() < 10 ? "0" + tempDate.getMonth()
							: Integer.toString(tempDate.getMonth());
					String formattedDate = (tempDate.getYear() + 1900) + "-" + month + "-" + day;
					HttpRequest request = HttpRequest.newBuilder()
							.uri(URI.create("https://fixer-fixer-currency-v1.p.rapidapi.com/" + formattedDate
									+ "?base=USD&symbols=BRL"))
							.header("X-RapidAPI-Key", "a9b6e16bbemshf3fbf9209955845p1ab613jsn2cdaf96d18f7")
							.header("X-RapidAPI-Host", "fixer-fixer-currency-v1.p.rapidapi.com")
							.method("GET", HttpRequest.BodyPublishers.noBody()).build();
					HttpResponse<String> response = HttpClient.newHttpClient().send(request,
							HttpResponse.BodyHandlers.ofString());
					ObjectMapper mapper = new ObjectMapper();
					JsonNode rootNode = mapper.readTree(response.body());
					allNodes.add(rootNode);
					tempDate.setTime(tempDate.getTime() + 24 * 60 * 60 * 1000L);
				}

			}
			return allNodes;
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}
		return null;
	}

}
