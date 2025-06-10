package com.crewmeister.cmcodingchallenge.security.auth;

import java.util.Collections;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crewmeister.cmcodingchallenge.security.dto.LoginRequest;
import com.crewmeister.cmcodingchallenge.security.dto.TokenDTO;
import com.crewmeister.cmcodingchallenge.util.AppConstants;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	/**
	 * POST /api/auth/token Validates the username and password and returns a JWT
	 * token if credentials are valid.
	 *
	 * @param request the login request containing username and password
	 * @return HTTP 200 with token if successful, or HTTP 401 with error message if
	 *         credentials are invalid
	 */
	@PostMapping("/token")
	public ResponseEntity<Object> getToken(@RequestBody LoginRequest request) {
		Optional<TokenDTO> tokenOpt = authService.getToken(request);

		// If credentials are valid, return the token
		if (tokenOpt.isPresent()) {
			return ResponseEntity.ok(tokenOpt.get());
		}

		// If credentials are invalid, return 401 Unauthorized with an error message
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Collections.singletonMap(AppConstants.ERROR, AppConstants.INVALID_CREDENTIALS));
	}
}
