package com.crewmeister.cmcodingchallenge.security;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crewmeister.cmcodingchallenge.security.dto.TokenDTO;
import com.crewmeister.cmcodingchallenge.util.ApplicationProperties;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Utility class responsible for generating and validating JWT tokens.
 */
@Component
public class JwtTokenProvider {

	private final ApplicationProperties applicationProperties;
	private final String JWT_SECRET;
	private final long JWT_EXPIRATION_MS = 86400000;

	@Autowired
	public JwtTokenProvider(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
		this.JWT_SECRET = this.applicationProperties.getSecurity().getJwtSecret();
	}

	public TokenDTO generateToken(String username) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION_MS);

		// Create and sign the JWT
		String token = Jwts.builder().setSubject(username) // Set the subject (username)
				.setIssuedAt(now) // Token issue timestamp
				.setExpiration(expiryDate) // Token expiration timestamp
				.signWith(SignatureAlgorithm.HS512, JWT_SECRET) // Sign with secret key
				.compact();

		// Return token and expiry in a DTO
		return new TokenDTO(token, expiryDate);
	}

	public String getUsernameFromJWT(String token) {
		return Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(token).getBody().getSubject();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(token);
			return true; // Token is valid
		} catch (JwtException ex) {
			// Token is invalid (expired, malformed, etc.)
			return false;
		}
	}
}
