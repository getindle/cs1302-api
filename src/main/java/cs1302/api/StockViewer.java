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

import cs1302.api.AlphaResult;
import cs1302.api.AlphaResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class StockViewer extends VBox {

    /** HTTP client. */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns a HttpClient object

    /** Google {@code Gson} object for parsing JSON-formatted strings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()                          // enable nice output when printing
        .create();                                    // builds and returns a Gson object

    VBox mainBox;
    HBox urlLayer;
    TextField url;
    ImageView imgView;
    Label searchLabel;
    HBox rightsBox;
    Label rightsLabel;

    CategoryAxis xAxis;
    NumberAxis yAxis;
    LineChart<String, Number> exampleChart;

    AlphaResponse alphaRespVar;
    NewsResponse newsRespVar;
    boolean compareClicked = false;

    public StockViewer() {
        super();
        urlLayer = new HBox(8);
        url = new TextField("TSLA");
        imgView =  new ImageView();
        searchLabel = new Label("Search for Stock 1:");
        mainBox = new VBox();

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

        HBox.setHgrow(url, Priority.ALWAYS);
        urlLayer.setAlignment(Pos.CENTER);
        searchLabel.setAlignment(Pos.CENTER);

        mainBox.setPrefSize(500, 400);
        mainBox.getChildren().add(exampleChart);
        this.getChildren().addAll(urlLayer, mainBox);
        urlLayer.getChildren().addAll(searchLabel, url);

        Image bannerImage = new Image("file:resources/readme-banner.png");
        imgView.setImage(bannerImage);
        imgView.setPreserveRatio(true);
        imgView.setFitWidth(300);
    }

    public void chartHttpConnect() throws NullPointerException {
        compareClicked = true;
        String alphaUrl =
            "https://api.polygon.io/v2/aggs/ticker/";
        String searchVal = URLEncoder.encode(url.getText().toUpperCase(), StandardCharsets.UTF_8);
        String alphaKey = "/range/1/day/2022-04-26/2023-04-26?adjusted=true" +
            "&sort=asc&apiKey=M8LEPO1yBQpxSmJur8RY3BnoAGa1fENo";
        String encAlphaUrl = "";
        encAlphaUrl = alphaUrl + searchVal + alphaKey;


        URI alphaVan = URI.create(encAlphaUrl);
        HttpRequest request = HttpRequest.newBuilder().uri(alphaVan).build();


        try {

            HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());

            String responseBody = response.body();

            AlphaResponse alphaResp = GSON.fromJson(responseBody, AlphaResponse.class);
            this.alphaRespVar = alphaResp;

            if (alphaResp.results == null) {
                mainBox.getChildren().clear();
                alphaRespVar = null;
                throw new NullPointerException
                ("One or more stock tickers are invalid.\nPlease try again.");
            }
            LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setMinSize(500, 400);
            lineChart.setMaxSize(500, 400);
            lineChart.setLegendVisible(false);
            lineChart.setCreateSymbols(false);
//            mainBox.getChildren().clear();
            String[] dates = new String[alphaResp.results.size()];
            double[] closePrices = new double[alphaResp.results.size()];
            for (int i = 0; i < alphaResp.results.size(); i++) {
                AlphaResult stockData = alphaResp.results.get(i);
                dates[i] = stockData.t;
                closePrices[i] = stockData.c;
            }

            xAxis.setLabel("Year To Date (YTD)");
            yAxis.setLabel("Prices");
            lineChart.setTitle(url.getText().toUpperCase());
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (int i = 0; i < closePrices.length; i++) {
                String date = dates[i];
                double cPrice = closePrices[i];
                XYChart.Data<String, Number> newData = new XYChart.Data<>(date, cPrice);
                series.getData().add(newData);
            }
            lineChart.getData().add(series);
            mainBox.getChildren().clear();
            mainBox.getChildren().add(lineChart);
        } catch (IOException | InterruptedException e) {
            mainBox.getChildren().clear();
            alphaRespVar = null;
            throw new NullPointerException
            ("Chart data was disrupted.\nPlease check your input and try again.");
        }
    }

    public void newsHttpConnect() {
        String newsUrl =
            "https://newsapi.org/v2/everything?q=";
        String searchVal = URLEncoder.encode(url.getText(), StandardCharsets.UTF_8);
        String newsKey = "&apikey=a8db905a07b64fdb9605bf06b44844bd";
        String encNewsUrl = "";
        encNewsUrl = newsUrl + searchVal + newsKey;
        URI newsVan = URI.create(encNewsUrl);
        HttpRequest request = HttpRequest.newBuilder().uri(newsVan).build();
        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
            String responseBody = response.body();
            NewsResponse newsResp = GSON.fromJson(responseBody, NewsResponse.class);
            this.newsRespVar = newsResp;
            if (newsResp.articles.size() < 5) {
                throw new NullPointerException
                ("No relevant articles found.\nPlease try a different ticker to see the news.");
            }
            VBox newsBox = new VBox();
            newsBox.setPrefWidth(480);
            ScrollPane sp = new ScrollPane();
            sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            sp.setPrefSize(500, 400);
            mainBox.getChildren().clear();
            for (int i = 0; i < 10 && i < newsResp.articles.size(); i++) {
                HBox fullArt = new HBox();
                NewsResult article = newsResp.articles.get(i);
                VBox artText = new VBox(10);
                Label headline = new Label(article.title);
                headline.setWrapText(true);
                headline.setPrefWidth(sp.getPrefWidth());
                headline.setStyle("-fx-font-family: Arial;" +
                    "-fx-font-weight: bold; -fx-font-size: 12pt;");
                Label artUrl = new Label(article.url);
                artUrl.setWrapText(true);
                artUrl.setPrefWidth(sp.getPrefWidth());
                artUrl.setStyle("-fx-font-family: TimesNewRoman; -fx-text-fill: #4285F4;");
                artText.getChildren().addAll(headline, artUrl);
                artText.setPadding(new Insets(0, 0, 20, 0));
                fullArt.getChildren().addAll(artText);
                fullArt.setStyle("-fx-background-color: #FFFFFF;" +
                    "-fx-border-color: #d9d9d9; -fx-border-width: 1px;");
                newsBox.getChildren().add(fullArt);
            }
            sp.setContent(newsBox);
            mainBox.getChildren().add(sp);
        } catch (IOException | InterruptedException e) {
            throw new NullPointerException
            ("Chart data was disrupted.\nPlease check your input and try again.");
        }
    }

    public void setSearchLabel(String s) {
        searchLabel.setText(s);
    }
}
