package com.crewmeister.cmcodingchallenge.security.auth;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crewmeister.cmcodingchallenge.security.JwtTokenProvider;
import com.crewmeister.cmcodingchallenge.security.dto.LoginRequest;
import com.crewmeister.cmcodingchallenge.security.dto.TokenDTO;
import com.crewmeister.cmcodingchallenge.util.ApplicationProperties;

@Service
public class AuthServiceImpl implements AuthService {

	private final JwtTokenProvider jwtTokenProvider;
	private final ApplicationProperties applicationProperties;

	@Autowired
	public AuthServiceImpl(JwtTokenProvider jwtTokenProvider, ApplicationProperties applicationProperties) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.applicationProperties = applicationProperties;
	}

	@Override
	public Optional<TokenDTO> getToken(LoginRequest request) {
		// Validate the provided credentials with the values from application.properties
		if (this.applicationProperties.getSecurity().getUsername().equals(request.getUsername())
				&& this.applicationProperties.getSecurity().getPassword().equals(request.getPassword())) {

			// If valid, generate and return a JWT token
			return Optional.of(jwtTokenProvider.generateToken(request.getUsername()));
		} else {
			// Return empty if authentication fails
			return Optional.empty();
		}
	}
}
