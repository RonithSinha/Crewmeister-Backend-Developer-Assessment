package com.crewmeister.cmcodingchallenge.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CurrencyService {

	/**
	 * Fetches exchange rate data for all supported currencies.
	 * 
	 * @return Map of currency code to map of LocalDate to exchange rate.
	 */
	Map<String, Map<LocalDate, Double>> fetchAllCurrenciesData();

	/**
	 * Loads a list of supported currency codes.
	 * 
	 * @return List of supported currency codes (e.g., "USD", "GBP", etc.)
	 */
	List<String> getCurrenciesList();


}
