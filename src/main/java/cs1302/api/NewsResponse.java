package cs1302.api;

import java.util.List;

/**
 * Represents a response from the NewsAPI. This is used by Gson
 * to create an object from the JSON response body.
 */
public class NewsResponse {
    private String status;
    int totalResults;
    List<NewsResult> articles;
} // NewsResponse
