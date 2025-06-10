package com.crewmeister.cmcodingchallenge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.crewmeister.cmcodingchallenge.service.CurrencyService;
import com.crewmeister.cmcodingchallenge.service.impl.ExchangeRateServiceImpl;
import com.crewmeister.cmcodingchallenge.util.AppConstants;
import com.crewmeister.cmcodingchallenge.util.CacheService;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateServiceTest {

	@Mock
	private CurrencyService currencyService;

	@Mock
	private CacheService cacheService;

	@InjectMocks
	private ExchangeRateServiceImpl exchangeRateService;

	private final LocalDate today = LocalDate.now();
	private final LocalDate pastDate = today.minusDays(1);

	/**
	 * Set up mock data before each test. Prepares a mock CurrencyMap containing
	 * rates for USD and INR for two dates.
	 */
	@BeforeEach
	void setUp() {
		Map<LocalDate, Double> usdRates = new LinkedHashMap<>();
		usdRates.put(pastDate, 1.2);
		usdRates.put(today, 1.1);

		Map<LocalDate, Double> inrRates = new LinkedHashMap<>();
		inrRates.put(pastDate, 98.4);
		inrRates.put(today, 98.6);

		Map<String, Map<LocalDate, Double>> currencyData = new LinkedHashMap<>();
		currencyData.put("USD", usdRates);
		currencyData.put("INR", inrRates);

		// Lenient mocking of service methods to suppress UnnecessaryStubbingException
		lenient().when(currencyService.fetchAllCurrenciesData()).thenReturn(currencyData);
		lenient().when(currencyService.getCurrenciesList()).thenReturn(List.of("USD", "INR"));
	}

	/**
	 * Test to verify that all exchange rates are fetched correctly.
	 */
	@Test
	void testGetAllRates() {
		Map<String, Map<LocalDate, Double>> currencyMap = exchangeRateService.getAllRates();
		assertNotNull(currencyMap);
		assertTrue(currencyMap.containsKey("USD"));
		assertTrue(currencyMap.containsKey("INR"));
	}

	/**
	 * Test retrieving exchange rates for a valid past date.
	 */
	@Test
	void testGetRatesByDate_validDate() throws Exception {
		Map<String, Double> rates = exchangeRateService.getRatesByDate(pastDate);
		assertNotNull(rates);
		assertEquals(1.2, rates.get("USD"));
		assertEquals(98.4, rates.get("INR"));
	}

	/**
	 * Test that requesting rates for a future date throws an exception.
	 */
	@Test
	void testGetRatesByDate_futureDate_throwsException() {
		LocalDate futureDate = today.plusDays(1);
		assertThrows(Exception.class, () -> exchangeRateService.getRatesByDate(futureDate));
	}

	/**
	 * Test retrieving individual exchange rates for specific currencies and a valid
	 * date.
	 */
	@Test
	void testGetRateByCurrencyAndDate_found() throws Exception {
		Optional<Double> rateUSD = exchangeRateService.getRateByCurrencyAndDate("USD", pastDate);
		assertTrue(rateUSD.isPresent());
		assertEquals(1.2, rateUSD.get());

		Optional<Double> rateINR = exchangeRateService.getRateByCurrencyAndDate("INR", pastDate);
		assertTrue(rateINR.isPresent());
		assertEquals(98.4, rateINR.get());
	}

	/**
	 * Test that an exception is thrown when the requested date is in the future.
	 */
	@Test
	void testGetRateByCurrencyAndDateNotFound() throws Exception {
		Exception exception = assertThrows(Exception.class, () -> {
			exchangeRateService.getRateByCurrencyAndDate("USD", today.plusDays(5));
		});
		String expectedMessage = AppConstants.PAST_DATE_WARNING;
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}

	/**
	 * Test currency conversion to EUR for a valid positive amount.
	 */
	@Test
	void testConvertToEur_valid() throws Exception {
		Optional<Double> amount = exchangeRateService.convertToEur("USD", pastDate, 12.0);
		assertTrue(amount.isPresent());
		assertEquals(10.0, amount.get());
	}

	/**
	 * Test that conversion returns empty for a negative amount.
	 */
	@Test
	void testConvertToEur_negativeAmount() throws Exception {
		Optional<Double> amount = exchangeRateService.convertToEur("USD", pastDate, -5.0);
		assertFalse(amount.isPresent());
	}

	/**
	 * Test retrieval of all available currencies.
	 */
	@Test
	void testGetAllCurrencies() {
		List<String> currencies = exchangeRateService.getAllCurrencies();
		assertEquals(List.of("USD", "INR"), currencies);
	}

	/**
	 * Test that fetching the latest data clears the cache and refreshes currency
	 * data.
	 */
	@Test
	void testFetchLatestData() {
		exchangeRateService.fetchLatestData();
		verify(cacheService).evictCaches(List.of(AppConstants.CURRENCY_CACHE));
		verify(currencyService).fetchAllCurrenciesData();
	}
}
