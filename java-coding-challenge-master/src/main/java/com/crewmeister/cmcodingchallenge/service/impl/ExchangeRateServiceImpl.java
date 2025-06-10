package com.crewmeister.cmcodingchallenge.service.impl;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.crewmeister.cmcodingchallenge.service.CurrencyService;
import com.crewmeister.cmcodingchallenge.service.ExchangeRateService;
import com.crewmeister.cmcodingchallenge.util.AppConstants;
import com.crewmeister.cmcodingchallenge.util.CacheService;
import com.crewmeister.cmcodingchallenge.util.CommonUtil;

@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {

	private final CacheService cacheService;
	private final CurrencyService currencyService;

	@Autowired
	public ExchangeRateServiceImpl(CurrencyService currencyService, CacheService cacheService) {
		this.currencyService = currencyService;
		this.cacheService = cacheService;
	}

	/**
	 * Event listener method that triggers once the application is fully started. It
	 * loads and caches the exchange rate data for all currencies at startup to
	 * improve performance for subsequent requests.
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void loadCurrencyExchangeRateData() {
		currencyService.fetchAllCurrenciesData();
	}

	/**
	 * Retrieves exchange rate data for all currencies and all available dates.
	 * Delegates to the CurrencyService to fetch and cache the data.
	 * 
	 * @return Map where key is currency code, and value is a map of date to
	 *         exchange rate.
	 */
	public Map<String, Map<LocalDate, Double>> getAllRates() {
		return currencyService.fetchAllCurrenciesData();
	}

	/**
	 * Retrieves exchange rates for all currencies on a specific date.
	 * 
	 * @param date The date for which exchange rates are requested.
	 * @return Map where key is currency code and value is exchange rate on given
	 *         date.
	 * @throws Exception if the requested date is in the future.
	 */
	public Map<String, Double> getRatesByDate(LocalDate date) throws Exception {
		if (!date.isBefore(LocalDate.now())) {
			throw new Exception(AppConstants.PAST_DATE_WARNING);
		}

		Map<String, Double> result = new LinkedHashMap<>();

		// Iterate over all currencies and fetch the rate for the requested date
		for (Map.Entry<String, Map<LocalDate, Double>> entry : currencyService.fetchAllCurrenciesData().entrySet()) {
			Map<LocalDate, Double> currencyMap = entry.getValue();
			Double rate = currencyMap.get(date); // May be null if no rate available for the date
			result.put(entry.getKey(), rate);
		}

		return result;
	}

	/**
	 * Retrieves the exchange rate for a specific currency on a given date.
	 * 
	 * @param currency The currency code (e.g., "USD").
	 * @param date     The date for which the exchange rate is requested.
	 * @return Optional containing the exchange rate if found, otherwise empty.
	 * @throws Exception if the requested date is in the future.
	 */
	public Optional<Double> getRateByCurrencyAndDate(String currency, LocalDate date) throws Exception {
		if (!date.isBefore(LocalDate.now())) {
			throw new Exception(AppConstants.PAST_DATE_WARNING);
		}

		// Get the map of date->rate for the given currency
		Map<LocalDate, Double> currencyData = currencyService.fetchAllCurrenciesData().get(currency);

		if (currencyData != null) {
			// Search for the exact date and return its rate if present
			for (Entry<LocalDate, Double> entry : currencyData.entrySet()) {
				if (entry.getKey().equals(date)) {
					return Optional.of(entry.getValue());
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * Converts a given amount from a specified currency to EUR based on the
	 * exchange rate on a given date.
	 * 
	 * @param currency The currency code of the amount.
	 * @param date     The date for the exchange rate.
	 * @param amount   The amount in the specified currency.
	 * @return Optional containing the converted amount in EUR, or empty if
	 *         conversion is not possible.
	 * @throws Exception if the requested date is in the future.
	 */
	public Optional<Double> convertToEur(String currency, LocalDate date, double amount) throws Exception {
		if (amount < 0) {
			return Optional.empty(); // Negative amounts cannot be converted
		}

		// Fetch the exchange rate for the currency on the given date
		Optional<Double> currencyRateOpt = getRateByCurrencyAndDate(currency, date);

		if (currencyRateOpt.isPresent()) {
			double currencyRate = currencyRateOpt.get();

			// Conversion formula: amount in EUR = amount in currency / exchange rate
			double amountEur = CommonUtil.roundToTwoDecimalPlaces(amount / currencyRate);

			return Optional.of(amountEur);
		}

		return Optional.empty();
	}

	/**
	 * Retrieves the list of all supported currency codes. Delegates to the
	 * CurrencyService.
	 * 
	 * @return List of currency codes as strings (e.g., ["USD", "GBP", "CHF"])
	 */
	public List<String> getAllCurrencies() {
		return currencyService.getCurrenciesList();
	}

	/**
	 * Evicts the currency cache and fetches the latest exchange data to refresh the
	 * cache. Useful to update rates during application runtime.
	 */
	public void fetchLatestData() {
		cacheService.evictCaches(List.of(AppConstants.CURRENCY_CACHE));
		currencyService.fetchAllCurrenciesData();
	}

	/**
	 * Alias method to fetch exchange rate data for all currencies and all dates.
	 * 
	 * @return Map of currency to date-rate map.
	 */
	public Map<String, Map<LocalDate, Double>> getAllCurrenciesData() {
		return currencyService.fetchAllCurrenciesData();
	}
}
