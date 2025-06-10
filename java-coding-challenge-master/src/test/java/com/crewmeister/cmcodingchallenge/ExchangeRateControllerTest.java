package com.crewmeister.cmcodingchallenge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.util.NestedServletException;

import com.crewmeister.cmcodingchallenge.controller.ExchangeRateController;
import com.crewmeister.cmcodingchallenge.service.ExchangeRateService;

/**
 * Integration tests for the ExchangeRateController using MockMvc. This class
 * focuses on testing the controller layer by mocking the service layer.
 */
@WebMvcTest(ExchangeRateController.class)
@WithMockUser(username = "CrewMeister", password = "A0OWnJfgLR") // Simulates authenticated user
public class ExchangeRateControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ExchangeRateService exchangeRateService;

	private final LocalDate sampleDate = LocalDate.of(2023, 12, 1);

	@BeforeEach
	void setup() {
		// No setup needed currently; retained for extensibility.
	}

	/**
	 * Tests the /currencies endpoint for returning a list of currencies.
	 */
	@Test
	public void testGetCurrencies() throws Exception {
		List<String> currencies = List.of("USD", "INR");
		when(exchangeRateService.getAllCurrencies()).thenReturn(currencies);

		mockMvc.perform(MockMvcRequestBuilders.get("/api/exchange-rates/currencies")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0]").value("USD")).andExpect(jsonPath("$[1]").value("INR"));
	}

	/**
	 * Tests the /api/exchange-rates endpoint to get all currency rates.
	 */
	@Test
	public void testGetAllRates() throws Exception {
		Map<String, Map<LocalDate, Double>> ratesMap = Map.of("USD", Map.of(LocalDate.parse("2023-12-01"), 1.2));
		Mockito.when(exchangeRateService.getAllRates()).thenReturn(ratesMap);

		mockMvc.perform(MockMvcRequestBuilders.get("/api/exchange-rates")).andExpect(status().isOk())
				.andExpect(jsonPath("$.USD.2023-12-01").value(1.2));
	}

	/**
	 * Tests the endpoint that fetches rates for a specific date.
	 */
	@Test
	public void testGetRatesByDate() throws Exception {
		Map<String, Double> rates = Map.of("USD", 1.2, "INR", 80.5);
		when(exchangeRateService.getRatesByDate(sampleDate)).thenReturn(rates);

		mockMvc.perform(MockMvcRequestBuilders.get("/api/exchange-rates/date/2023-12-01")).andExpect(status().isOk())
				.andExpect(jsonPath("$.USD").value(1.2)).andExpect(jsonPath("$.INR").value(80.5));
	}

	/**
	 * Tests the endpoint to get a single exchange rate for a given currency and
	 * date.
	 */
	@Test
	public void testGetRateForCurrencyAndDate() throws Exception {
		when(exchangeRateService.getRateByCurrencyAndDate("USD", sampleDate)).thenReturn(Optional.of(1.23));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/exchange-rates/USD/date/2023-12-01"))
				.andExpect(status().isOk()).andExpect(content().string("1.23"));
	}

	/**
	 * Tests the conversion to EUR from a given currency and date.
	 */
	@Test
	public void testConvertToEur() throws Exception {
		when(exchangeRateService.convertToEur("INR", sampleDate, 160.0)).thenReturn(Optional.of(2.0));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/exchange-rates/INR/convert-to-eur/2023-12-01/160"))
				.andExpect(status().isOk()).andExpect(content().string("2.0"));
	}

	/**
	 * Tests that an exception is thrown when the requested currency rate is not
	 * found.
	 */
	@Test
	public void testRateForCurrencyNotFound() throws Exception {
		when(exchangeRateService.getRateByCurrencyAndDate("ABC", sampleDate)).thenReturn(Optional.empty());

		NestedServletException thrown = assertThrows(NestedServletException.class, () -> {
			mockMvc.perform(MockMvcRequestBuilders.get("/api/exchange-rates/ABC/date/2023-12-01"));
		});

		Throwable rootCause = thrown.getRootCause();
		assert rootCause instanceof RuntimeException;
		assert "Exchange rate unavailable".equals(rootCause.getMessage());
	}

	/**
	 * Tests that an exception is thrown when conversion to EUR is unavailable.
	 */
	@Test
	public void testConvertToEurUnavailable() throws Exception {
		when(exchangeRateService.convertToEur("XYZ", sampleDate, 100.0)).thenReturn(Optional.empty());

		NestedServletException thrown = assertThrows(NestedServletException.class, () -> {
			mockMvc.perform(MockMvcRequestBuilders.get("/api/exchange-rates/XYZ/convert-to-eur/2023-12-01/100"));
		});

		Throwable rootCause = thrown.getRootCause();
		assertTrue(rootCause instanceof RuntimeException);
		assertEquals("Conversion unavailable", rootCause.getMessage());
	}
}
