package cs1302.api;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a result in a response from the Alpha Vantage API. This is used by GSON
 * to create an object from the JSON response body.
 */
public class AlphaResult {
/*    @SerializedName("4. Close")
    String close;
*/
    public double c; // closing price
    public String t; // timestamp
} // AlphaResult
