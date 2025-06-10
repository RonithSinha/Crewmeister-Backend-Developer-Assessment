package com.crewmeister.cmcodingchallenge.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ExchangeRateService {

    /**
     * Retrieves exchange rate data for all currencies and all dates.
     *
     * @return Map of currency code to (date -> rate) map.
     */
    Map<String, Map<LocalDate, Double>> getAllRates();

    /**
     * Retrieves exchange rate data for all currencies on a specific date.
     *
     * @param date The date for which to fetch exchange rates.
     * @return Map of currency code to rate on the given date.
     * @throws Exception If the date is in the future.
     */
    Map<String, Double> getRatesByDate(LocalDate date) throws Exception;

    /**
     * Retrieves exchange rate for a specific currency and date.
     *
     * @param currency The currency code.
     * @param date     The date for which to fetch the rate.
     * @return Optional containing the exchange rate, or empty if not found.
     * @throws Exception If the date is in the future.
     */
    Optional<Double> getRateByCurrencyAndDate(String currency, LocalDate date) throws Exception;

    /**
     * Converts an amount from a given currency to EUR on a specific date.
     *
     * @param currency The currency code.
     * @param date     The date for conversion.
     * @param amount   The amount in the given currency.
     * @return Optional containing the converted amount in EUR, or empty if conversion not possible.
     * @throws Exception If the date is in the future.
     */
    Optional<Double> convertToEur(String currency, LocalDate date, double amount) throws Exception;

    /**
     * Fetches the list of all supported currency codes.
     *
     * @return List of currency codes.
     */
    List<String> getAllCurrencies();

    /**
     * Evicts cache and fetches latest exchange data.
     */
    void fetchLatestData();

    /**
     * Fetches exchange rate data for all currencies.
     *
     * @return Map of currency code to (date -> rate) map.
     */
    Map<String, Map<LocalDate, Double>> getAllCurrenciesData();
}
