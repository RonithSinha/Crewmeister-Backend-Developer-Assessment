package com.crewmeister.cmcodingchallenge.util;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI().info(new Info().title("CrewMeister Currency Exchange API").version("1.0.0")
				.description("API for currency exchange rates and conversion")
				.contact(new Contact().name("CrewMeister Support").email("support@crewmeister.com")
						.url("https://crewmeister.com"))
				.license(new License().name("Apache 2.0").url("http://springdoc.org")));
	}
}
