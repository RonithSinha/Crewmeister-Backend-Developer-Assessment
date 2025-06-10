package com.crewmeister.cmcodingchallenge.security;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.crewmeister.cmcodingchallenge.util.AppConstants;

/**
 * Filter that intercepts each HTTP request to validate JWT tokens. If a valid
 * token is found, it sets the authentication in the SecurityContext.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider tokenProvider;

	public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	/**
	 * Intercepts incoming requests to extract and validate JWT tokens. Sets
	 * authentication in the SecurityContext if the token is valid. If invalid or
	 * missing, returns 401 Unauthorized response.
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// Extract JWT token from Authorization header
		String token = getJWTFromRequest(request);

		// Validate token and authenticate user
		if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
			String username = tokenProvider.getUsernameFromJWT(token);

			// Set authentication with empty authorities (roles not used here)
			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null,
					Collections.emptyList());
			auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

			// Set authentication in SecurityContext
			SecurityContextHolder.getContext().setAuthentication(auth);

			// Proceed with the request
			filterChain.doFilter(request, response);
		} else {
			// If token is invalid or missing, return 401 Unauthorized
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			String json = String.format("{\"error\": \"%s\", \"message\": \"%s\"}", AppConstants.UNAUTHORIZED,
					AppConstants.INVALID_TOKEN_ERROR);
			response.getWriter().write(json);
		}
	}
	
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
	    String path = request.getRequestURI();

	    // Define endpoints that should NOT be filtered (e.g., public or auth endpoints)
	    // Add any other endpoints you want to exclude here
	    return path.equals("/api/auth/token") 
	            || path.startsWith("/swagger-ui") 
	            || path.startsWith("/v3/api-docs")
	            || path.startsWith("/actuator");
	}

	private String getJWTFromRequest(HttpServletRequest request) {
		String bearer = request.getHeader("Authorization");
		if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
			return bearer.substring(7); // Remove "Bearer " prefix
		}
		return null;
	}
}
