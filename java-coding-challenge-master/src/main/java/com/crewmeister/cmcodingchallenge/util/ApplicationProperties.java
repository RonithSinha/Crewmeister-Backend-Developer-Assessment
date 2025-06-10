package com.crewmeister.cmcodingchallenge.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "app")
@Data
public class ApplicationProperties {

	private Security security = new Security();
	private ExchangeRateData exchangeRateData = new ExchangeRateData();

	@Data
	public static class Security {
		private String username;
		private String password;
		private String jwtSecret;
	}

	@Data
	public static class ExchangeRateData {
		private String url;
	}
}
