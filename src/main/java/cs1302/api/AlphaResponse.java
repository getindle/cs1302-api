package cs1302.api;

import java.util.List;

/**
 * Represents a response from the Alpha Vantage API. This is used by GSON to create
 * an object from the JSON response body.
 */
public class AlphaResponse {
    public String ticker;
    public int queryCount;
    public int resultsCount;
    public boolean adjusted;
    public List<AlphaResult> results;
} // AlphaResponse
