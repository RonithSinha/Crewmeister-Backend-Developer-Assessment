package com.crewmeister.cmcodingchallenge.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crewmeister.cmcodingchallenge.service.ExchangeRateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/exchange-rates")
public class ExchangeRateController {

	private final ExchangeRateService exchangeRateService;

	@Autowired
	public ExchangeRateController(ExchangeRateService exchangeRateService) {
		this.exchangeRateService = exchangeRateService;
	}

	/**
	 * GET /currencies Returns a list of all supported currency codes (e.g., USD,
	 * EUR).
	 */
	@GetMapping("/currencies")
	@Operation(summary = "Get supported currency codes", description = "Returns a list of all supported currency codes (e.g., USD, EUR).", responses = {
			@ApiResponse(responseCode = "200", description = "Successful retrieval", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "[\"USD\", \"EUR\", \"GBP\"]"))) })
	public ResponseEntity<List<String>> getCurrenciesList() {
		List<String> currencies = exchangeRateService.getAllCurrencies();
		return ResponseEntity.ok(currencies);
	}

	/**
	 * GET / Returns a map of all available exchange rates for all currencies and
	 * dates.
	 */
	@GetMapping
	@Operation(summary = "Get all exchange rates", description = "Returns a map of all available exchange rates for all currencies and dates.", responses = {
			@ApiResponse(responseCode = "200", description = "Successful retrieval", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"USD\": {\"2025-06-08\": 1.2, \"2025-06-09\": 1.1}, \"CHF\": {\"2025-06-08\": 0.95, \"2025-06-09\": 0.97}}"))) })
	public ResponseEntity<Map<String, Map<LocalDate, Double>>> getAllRates() {
		Map<String, Map<LocalDate, Double>> allRates = exchangeRateService.getAllRates();
		return ResponseEntity.ok(allRates);
	}

	/**
	 * GET /date/{date} Returns exchange rates for all currencies on the specified
	 * date.
	 *
	 * @param date in ISO format (yyyy-MM-dd)
	 */
	@GetMapping("/date/{date}")
	@Operation(summary = "Get exchange rates by date", description = "Returns exchange rates for all currencies on the specified date.", responses = {
			@ApiResponse(responseCode = "200", description = "Successful retrieval", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"USD\": 1.1, \"CHF\": 0.9}"))) })
	public ResponseEntity<Map<String, Double>> getRatesByDate(
			@Parameter(description = "Date in ISO format (yyyy-MM-dd)") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date)
			throws Exception {
		Map<String, Double> rates = exchangeRateService.getRatesByDate(date);
		return ResponseEntity.ok(rates);
	}

	/**
	 * GET /{currency}/date/{date} Returns the exchange rate for a specific currency
	 * on a given date.
	 *
	 * @param currency currency code like USD, INR
	 * @param date     date for which rate is requested
	 */
	@GetMapping("/{currency}/date/{date}")
	@Operation(summary = "Get exchange rate for a specific currency and date", description = "Returns the exchange rate for a given currency on a specified date.", responses = {
			@ApiResponse(responseCode = "200", description = "Successful retrieval", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "1.1"))),
			@ApiResponse(responseCode = "404", description = "Exchange rate unavailable") })
	public ResponseEntity<Double> getRateForCurrencyAndDate(
			@Parameter(description = "Currency code like USD, INR") @PathVariable String currency,
			@Parameter(description = "Date in ISO format (yyyy-MM-dd)") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date)
			throws Exception {
		return exchangeRateService.getRateByCurrencyAndDate(currency, date).map(ResponseEntity::ok)
				.orElseThrow(() -> new RuntimeException("Exchange rate unavailable"));
	}

	/**
	 * GET /{currency}/convert-to-eur/{date}/{amount} Converts a given amount in a
	 * specific currency to EUR using the exchange rate on the provided date.
	 *
	 * @param currency source currency code
	 * @param date     conversion date
	 * @param amount   amount in source currency to convert
	 */
	@GetMapping("/{currency}/convert-to-eur/{date}/{amount}")
	@Operation(summary = "Convert currency to EUR", description = "Converts the specified amount in a currency to EUR using the exchange rate on the provided date.", responses = {
			@ApiResponse(responseCode = "200", description = "Successful conversion", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "110.0"))),
			@ApiResponse(responseCode = "404", description = "Conversion unavailable") })
	public ResponseEntity<Double> convertToEur(
			@Parameter(description = "Source currency code") @PathVariable String currency,
			@Parameter(description = "Conversion date in ISO format (yyyy-MM-dd)") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@Parameter(description = "Amount in source currency to convert") @PathVariable double amount)
			throws Exception {
		return exchangeRateService.convertToEur(currency, date, amount).map(ResponseEntity::ok)
				.orElseThrow(() -> new RuntimeException("Conversion unavailable"));
	}

	/**
	 * Scheduled job to refresh the exchange rate data cache daily. Runs every day
	 * at 00:05 (assumes Bundesbank EUR-FX exchange rates data updates at midnight).
	 *
	 * Source:
	 * https://www.bundesbank.de/dynamic/action/en/statistics/time-series-databases/time-series-databases/759784/759784?statisticType=BBK_ITS&listId=www_sdks_b01012_3
	 */
	@Scheduled(cron = "0 5 0 * * *")
	@Operation(summary = "Scheduled refresh of exchange rates", description = "Scheduled job to refresh the exchange rate data cache daily at 00:05.")
	public void fetchLatestExchangeRateData() {
		log.info("Running scheduled job to fetch latest exchange rate data: " + java.time.LocalDateTime.now());
		exchangeRateService.fetchLatestData();
		log.info("Job to fetch latest exchange rate data completed");
	}
}
