package com.crewmeister.cmcodingchallenge.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.opencsv.CSVReader;

@Component
public class CommonUtil {

	public static double roundToTwoDecimalPlaces(double value) {
		return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
	
	public static CSVReader parseCSV(String url) throws MalformedURLException, IOException {
		return new CSVReader(new InputStreamReader(new URL(url).openStream()));
	}

	public static Optional<LocalDate> parseLocalDate(String date) {
		try {
			return Optional.of(LocalDate.parse(date));
		} catch (Exception e) {
			// TODO: handle exception
			return Optional.empty();
		}
	}

	public static Optional<Double> parseDouble(String str) {
		if (str == null || str.isEmpty())
			return Optional.empty();
		try {
			double parsedValue = Double.parseDouble(str);
			return Optional.of(parsedValue);
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}
}
