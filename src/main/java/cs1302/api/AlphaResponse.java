package cs1302.api;

import java.util.Map;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents a response from the Alpha Vantage API. This is used by GSON to create
 * an object from the JSON response body.
 */
public class AlphaResponse {
/*    @SerializedName("Time Series (Daily)")
    Map<String, AlphaResult> series;
*/
    public String ticker;
    public int queryCount;
    public int resultsCount;
    public boolean adjusted;
    public List<AlphaResult> results;
} // AlphaResponse
