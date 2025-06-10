package com.crewmeister.cmcodingchallenge.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration class that sets up JWT-based stateless authentication.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtTokenProvider tokenProvider;

	public SecurityConfig(JwtTokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter(tokenProvider);
	}

	/**
	 * Configures the security filter chain to: - Disable CSRF - Use stateless
	 * session management - Allow unauthenticated access to the authentication
	 * endpoint - Require authentication for all other endpoints - Add the JWT
	 * filter before the standard username-password filter
	 *
	 * @param http the HttpSecurity to modify
	 * @return configured SecurityFilterChain
	 * @throws Exception in case of misconfiguration
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf().disable() // Disable CSRF protection (not needed for stateless APIs)
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests()
				.antMatchers("/api/auth/token", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
						"/swagger-resources/**", "/webjars/**")
				.permitAll() // Allow open access to token endpoint
				.anyRequest().authenticated(); // All other requests require authentication

		// Add the custom JWT authentication filter before the standard Spring Security
		// filter
		http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
