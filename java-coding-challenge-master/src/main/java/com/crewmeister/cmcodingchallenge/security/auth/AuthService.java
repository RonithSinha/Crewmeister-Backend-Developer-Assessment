package com.crewmeister.cmcodingchallenge.security.auth;

import java.util.Optional;

import com.crewmeister.cmcodingchallenge.security.dto.LoginRequest;
import com.crewmeister.cmcodingchallenge.security.dto.TokenDTO;

public interface AuthService {
    Optional<TokenDTO> getToken(LoginRequest request);
}
