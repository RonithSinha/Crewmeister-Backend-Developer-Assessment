package com.crewmeister.cmcodingchallenge;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.crewmeister.cmcodingchallenge.security.dto.LoginRequest;
import com.crewmeister.cmcodingchallenge.util.ApplicationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class ExchangeRateIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	private String jwtToken;

	private final ApplicationProperties applicationProperties;

	@Autowired
	public ExchangeRateIntegrationTest(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}

	@BeforeEach
	void authenticate() throws Exception {
		LoginRequest loginRequest = new LoginRequest(this.applicationProperties.getSecurity().getUsername(),
				this.applicationProperties.getSecurity().getPassword());

		ObjectMapper mapper = new ObjectMapper();
		String loginRequestJson = mapper.writeValueAsString(loginRequest);

		// Perform login and extract JWT token from response
		String response = mockMvc
				.perform(post("/api/auth/token").contentType(MediaType.APPLICATION_JSON).content(loginRequestJson))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		// Assuming response contains token JSON like { "token": "jwt-token-string" }

		jwtToken = mapper.readTree(response).get("token").asText();
	}

	@Test
	void shouldReturnListOfCurrencies() throws Exception {
		System.out.println(jwtToken);
		mockMvc.perform(get("/api/exchange-rates/currencies").header("Authorization", "Bearer " + jwtToken))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0]").isString()); // assuming at least one currency
																					// exists
	}

	@Test
	void shouldReturnAllRates() throws Exception {
		mockMvc.perform(get("/api/exchange-rates").header("Authorization", "Bearer " + jwtToken))
				.andExpect(status().isOk());
	}

	@Test
	void shouldReturnRatesByDate() throws Exception {
		String date = LocalDate.now().minusDays(3).toString();
		mockMvc.perform(get("/api/exchange-rates/date/" + date).header("Authorization", "Bearer " + jwtToken))
				.andExpect(status().isOk());
	}

	@Test
	void shouldReturnRateForCurrencyAndDate() throws Exception {
		String date = "2025-06-09";
		mockMvc.perform(get("/api/exchange-rates/USD/date/" + date).header("Authorization", "Bearer " + jwtToken))
				.andExpect(status().isOk());
	}

	@Test
	void shouldConvertToEur() throws Exception {
		String date = "2025-06-09";
		mockMvc.perform(get("/api/exchange-rates/USD/convert-to-eur/" + date + "/100").header("Authorization",
				"Bearer " + jwtToken)).andExpect(status().isOk());
	}
}
