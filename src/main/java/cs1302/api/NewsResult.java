package cs1302.api;

/**
 * Represents a result in a response from the NewsAPI. This is used by
 * Gson to create an object from the JSON response body.
 */
public class NewsResult {
    String author;
    String title;
    String description;
    String url;
    String urlToImage;
    String publishedAt;
    String content;
} // NewsResult
