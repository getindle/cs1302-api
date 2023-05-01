package cs1302.api;

/**
 * Represents a result in a response from the Alpha Vantage API. This is used by GSON
 * to create an object from the JSON response body.
 */
public class AlphaResult {
    public double c; // closing price
    public String t; // timestamp
} // AlphaResult
