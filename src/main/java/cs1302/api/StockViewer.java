package cs1302.api;

import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;

import java.net.http.HttpClient;
import java.io.IOException;
import java.lang.InterruptedException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.io.FileInputStream;

import cs1302.api.AlphaResult;
import cs1302.api.AlphaResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Custom component class that creates a {@code StockViewer} object which allows users
 * to search for data related to a stock of their choice. This class connects to the Polygon.io
 * API and NewsAPI to gather the necessary data.
 */
public class StockViewer extends VBox {

    // declare final api keys
    final String polygonApiKey;
    final String newsApiKey;

    /** HTTP client. */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns a HttpClient object

    /** Google {@code Gson} object for parsing JSON-formatted strings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()                          // enable nice output when printing
        .create();                                    // builds and returns a Gson object

    // declare variables to build the app skeleton
    VBox mainBox;
    HBox urlLayer;
    TextField url;
    ImageView imgView;
    Label searchLabel;
    HBox rightsBox;
    Label rightsLabel;

    // declare linechart
    CategoryAxis xAxis;
    NumberAxis yAxis;
    LineChart<String, Number> exampleChart;

    // declare various helper variables
    AlphaResponse alphaRespVar;
    NewsResponse newsRespVar;
    boolean compareClicked = false;
    String buttonName = "";

    /**
     * Creates a {@code StockViewer} object containing a search box and empty
     * placeholder {@code LineChart}.
     */
    public StockViewer() {
        super();
        // retrieve the api keys from the "resources/config.properties" file
        String configPath = "resources/config.properties";
        String tempPolygonApiKey = "";
        String tempNewsApiKey = "";
        try (FileInputStream configFileStream = new FileInputStream(configPath)) {
            Properties config = new Properties();
            config.load(configFileStream);
            tempPolygonApiKey = config.getProperty("polygonio.apikey");
            tempNewsApiKey = config.getProperty("newsapi.apikey");
        } catch (IOException ioe) {
            System.err.println(ioe);
            ioe.printStackTrace();
        }
        polygonApiKey = tempPolygonApiKey;
        newsApiKey = tempNewsApiKey;
        // construct variables to build app skeleton
        urlLayer = new HBox(8);
        url = new TextField("TSLA");
        imgView =  new ImageView();
        searchLabel = new Label("Search for Stock 1:");
        mainBox = new VBox();
        // construct empty linechart
        xAxis = new CategoryAxis();
        yAxis = new NumberAxis();
        exampleChart = new LineChart<>(xAxis, yAxis);
        exampleChart.setMinSize(500, 400);
        exampleChart.setMaxSize(500, 400);
        exampleChart.setLegendVisible(false);
        exampleChart.setCreateSymbols(false);
        xAxis.setTickLabelsVisible(false);

        this.alphaRespVar = null;
        this.newsRespVar = null;
        // center/align the urlLayer
        HBox.setHgrow(url, Priority.ALWAYS);
        urlLayer.setAlignment(Pos.CENTER);
        searchLabel.setAlignment(Pos.CENTER);
        // add to scene
        mainBox.setPrefSize(500, 400);
        mainBox.getChildren().add(exampleChart);
        this.getChildren().addAll(urlLayer, mainBox);
        urlLayer.getChildren().addAll(searchLabel, url);

        Image bannerImage = new Image("file:resources/readme-banner.png");
        imgView.setImage(bannerImage);
        imgView.setPreserveRatio(true);
        imgView.setFitWidth(300);
    }

    /**
     * Connects to the Polygon.io API to retrieve and graph the year-to-date closing prices
     * of a user's desired stock in a {@code LineChart}.
     */
    public void chartHttpConnect() throws NullPointerException {
        compareClicked = true;
        String alphaUrl =
            "https://api.polygon.io/v2/aggs/ticker/";
        String searchVal = URLEncoder.encode(url.getText().toUpperCase(), StandardCharsets.UTF_8);
        buttonName = url.getText().toUpperCase();
        String alphaKey = "/range/1/day/2022-05-03/2023-05-03?adjusted=true" +
            "&sort=asc&apiKey=" + polygonApiKey;
        String encAlphaUrl = "";
        encAlphaUrl = alphaUrl + searchVal + alphaKey; // this is the full Url inlcuding API key
        URI alphaVan = URI.create(encAlphaUrl);
        HttpRequest request = HttpRequest.newBuilder().uri(alphaVan).build(); // reqeust
        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
            String responseBody = response.body(); // receive response, then parse the responseBody
            AlphaResponse alphaResp = GSON.fromJson(responseBody, AlphaResponse.class);
            this.alphaRespVar = alphaResp;
            if (alphaResp.results == null) { // if parsed response is null, throw exception
                mainBox.getChildren().clear();
                alphaRespVar = null;
                throw new NullPointerException
                ("One or more stock tickers are invalid.\nPlease try again.\n\nNOTE: " +
                "If both tickers are valid, you may be out of API calls.\nPlease wait one minute," +
                " then try again.");
            }
            // build linechart
            LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setMinSize(500, 400);
            lineChart.setMaxSize(500, 400);
            lineChart.setLegendVisible(false);
            lineChart.setCreateSymbols(false);
            String[] dates = new String[alphaResp.results.size()];
            double[] closePrices = new double[alphaResp.results.size()];
            // for each date & closing price, add to its own array
            for (int i = 0; i < alphaResp.results.size(); i++) {
                AlphaResult stockData = alphaResp.results.get(i);
                dates[i] = stockData.t;
                closePrices[i] = stockData.c;
            }
            xAxis.setLabel("Year To Date (YTD)"); // set chart labels
            yAxis.setLabel("Prices");
            lineChart.setTitle(url.getText().toUpperCase());
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (int i = 0; i < closePrices.length; i++) { // add data to the chart
                String date = dates[i];
                double cPrice = closePrices[i];
                XYChart.Data<String, Number> newData = new XYChart.Data<>(date, cPrice);
                series.getData().add(newData);
            }
            // clear mainBox (where the old linechart is present), then add updated linechart
            lineChart.getData().add(series);
            mainBox.getChildren().clear();
            mainBox.getChildren().add(lineChart);
        } catch (IOException | InterruptedException e) { // catch checked exceptions
            mainBox.getChildren().clear();
            alphaRespVar = null;
            throw new NullPointerException
            ("Chart data was disrupted.\nPlease check your input and try again.");
        }
    }

    /**
     * Connects to the NewsAPI to retrieve and present 10 articles related to the desired
     * stock search by the user.
     */
    public void newsHttpConnect() throws NullPointerException {
        if (alphaRespVar.ticker == null) {
            throw new NullPointerException
            ("No relevant articles found.\nPlease try a different ticker to see the news.");
        }
        String newsUrl =
            "https://newsapi.org/v2/everything?q=";
        String searchVal = URLEncoder.encode(alphaRespVar.ticker, StandardCharsets.UTF_8);
        String newsKey = "&apikey=" + newsApiKey;
        String encNewsUrl = "";
        encNewsUrl = newsUrl + searchVal + newsKey; // this is the full Url including api key
        URI newsVan = URI.create(encNewsUrl);
        HttpRequest request = HttpRequest.newBuilder().uri(newsVan).build(); // request
        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
            String responseBody = response.body(); // recieve response, then parse the responseBody
            NewsResponse newsResp = GSON.fromJson(responseBody, NewsResponse.class);
            this.newsRespVar = newsResp;
            // if less than 5 articles found, throw exception
            if (newsResp.articles.size() < 5) {
                throw new NullPointerException
                ("No relevant articles found.\nPlease try a different ticker to see the news.");
            }
            // build the news box as a ScrollPane
            VBox newsBox = new VBox();
            newsBox.setPrefWidth(480);
            ScrollPane sp = new ScrollPane();
            sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            sp.setPrefSize(500, 400);
            mainBox.getChildren().clear();
            // for 10 top artcles found, or the size of the article's response
            for (int i = 0; i < 10 && i < newsResp.articles.size(); i++) {
                HBox fullArt = new HBox(); // overall hbox for the article
                NewsResult article = newsResp.articles.get(i); // get article
                VBox artText = new VBox(10); // vbox to stack article headline & url
                Label headline = new Label(article.title); // label holding the article headline
                headline.setWrapText(true);
                headline.setPrefWidth(sp.getPrefWidth()); // make headline look fancy
                headline.setStyle("-fx-font-family: Arial;" +
                    "-fx-font-weight: bold; -fx-font-size: 12pt;");
                Label artUrl = new Label(article.url); // label holding article url
                artUrl.setWrapText(true);
                artUrl.setPrefWidth(sp.getPrefWidth()); // make url look fancy (like a real url)
                artUrl.setStyle("-fx-font-family: TimesNewRoman; -fx-text-fill: #4285F4;");
                artText.getChildren().addAll(headline, artUrl);
                artText.setPadding(new Insets(0, 0, 20, 0));
                fullArt.getChildren().addAll(artText);
                fullArt.setStyle("-fx-background-color: #FFFFFF;" + // style the overall news box
                    "-fx-border-color: #d9d9d9; -fx-border-width: 1px;");
                newsBox.getChildren().add(fullArt);
            }
            sp.setContent(newsBox); // add to ScrollPane
            mainBox.getChildren().add(sp); // add ScrollPane to mainBox
        } catch (IOException | InterruptedException e) {
            throw new NullPointerException
            ("Chart data was disrupted.\nPlease check your input and try again.");
        }
    }

    /**
     * Sets the value of the search label.
     *
     * @param s the new search label
     */
    public void setSearchLabel(String s) {
        searchLabel.setText(s);
    }
}
