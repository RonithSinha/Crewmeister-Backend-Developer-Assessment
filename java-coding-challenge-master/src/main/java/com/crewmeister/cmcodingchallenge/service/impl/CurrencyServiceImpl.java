package com.crewmeister.cmcodingchallenge.service.impl;

import java.io.InputStream;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.crewmeister.cmcodingchallenge.service.CurrencyService;
import com.crewmeister.cmcodingchallenge.util.AppConstants;
import com.crewmeister.cmcodingchallenge.util.CommonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CurrencyServiceImpl implements CurrencyService {


	/**
	 * Fetches exchange rate data for all supported currencies. Caches the result to
	 * avoid repeated I/O and network calls.
	 *
	 * @return Map of currency code to date-rate map.
	 */
	@Cacheable(value = AppConstants.CURRENCY_CACHE)
	@Override
	public Map<String, Map<LocalDate, Double>> fetchAllCurrenciesData() {
		log.info("Fetching data for all currencies");
		List<String> currencies = this.getCurrenciesList();
		Map<String, Map<LocalDate, Double>> allCurrenciesMap = new LinkedHashMap<>();

		for (String currency : currencies) {
			Map<LocalDate, Double> currencyMap = this.parseCurrencyCSV(currency);
			allCurrenciesMap.put(currency, currencyMap);
		}
		log.info("Fetched data for all currencies");
		return allCurrenciesMap;
	}

	/**
	 * Parses CSV data from the configured Bundesbank URL template for a given
	 * currency. Extracts date and exchange rate pairs.
	 *
	 * @param currency Currency code (e.g., "USD")
	 * @return Map of LocalDate to exchange rate for the given currency.
	 */
	private Map<LocalDate, Double> parseCurrencyCSV(String currency) {
//		log.info("Fetching data for currency {}", currency);
		Map<LocalDate, Double> records = new LinkedHashMap<>();

		String url = MessageFormat.format(AppConstants.EXCHANGE_RATE_DATA_URL, currency);

		try {
			CSVReader reader = CommonUtil.parseCSV(url);
			String[] line;

			while ((line = reader.readNext()) != null) {
				if (line.length >= 2) {
					Optional<LocalDate> localDateOpt = CommonUtil.parseLocalDate(line[0]);
					Optional<Double> rateOpt = CommonUtil.parseDouble(line[1]);

					// Only add entries if both date and rate are valid
					if (rateOpt.isPresent() && localDateOpt.isPresent()) {
						records.put(localDateOpt.get(), rateOpt.get());
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(AppConstants.LOAD_FAILURE_MSG + " CSV", e);
		}

		return records;
	}

	/**
	 * Loads a list of supported currency codes from a local `currencies.json` file.
	 *
	 * @return List of currency codes (e.g., ["USD", "GBP", "CHF"])
	 */
	@Override
	public List<String> getCurrenciesList() {
		try {
			InputStream is = new ClassPathResource("currencies.json").getInputStream();
			ObjectMapper mapper = new ObjectMapper();

			// Deserialize JSON array into a list of strings
			List<String> currencies = mapper.readValue(is, new TypeReference<List<String>>() {
			});
			return currencies;
		} catch (Exception e) {
			throw new RuntimeException(AppConstants.LOAD_FAILURE_MSG + " currencies.json", e);
		}
	}
}
