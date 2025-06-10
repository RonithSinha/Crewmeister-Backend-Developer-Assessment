package com.crewmeister.cmcodingchallenge.security.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenDTO {
	private String token;
	private Date expiresDate;
}
