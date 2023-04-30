package cs1302.api;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javafx.scene.control.Separator;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.geometry.Insets;

/**
 * Utilizes the {@code StockView} custom component class to build an app called "StockMatch" which
 * allows the user to enter two valid stock tickers and see the side-by-side YTD closing prices on
 * a nicely graphed {@code LineChart}. The user is also provided the ability to see relevant news
 * related to their stocks of choice. This app's chart data is powered by the Polygon.io API and the
 * news data is powered by NewsAPI.
 */
public class ApiApp extends Application {
    Stage stage;
    Scene scene;
    VBox root;

    HBox titleLayer;
    Label titleLabel;

    HBox topLayer;
    Label instructLabel;

    HBox svHolder;
    StockViewer stockView1;
    StockViewer stockView2;
    Separator separator;

    HBox bottomLayer;
    Button newsButton1;
    Button newsButton2;
    Button compareButton;

//    HBox rightsBox = new HBox(8);
//    Label rightsLabel = new Label("StockMatch is not intended to provide professional " +
//        "financial advice. Seek professional advice before making financial decisions.");


    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new VBox();

        titleLayer = new HBox();
        titleLabel = new Label("StockMatch");

        topLayer = new HBox(10);
        topLayer.setPadding(new Insets(10, 0, 0, 0));
        instructLabel = new Label
        ("Directions: Enter 2 valid stock tickers, then compare them or see relevant news.");

        svHolder = new HBox();
        stockView1 = new StockViewer();
        stockView2 = new StockViewer();
        separator = new Separator();

        bottomLayer = new HBox();
        newsButton1 = new Button("Stock 1 News");
        newsButton2 = new Button("Stock 2 News");
        compareButton = new Button("Compare");
    } // ApiApp

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

        this.stage = stage;
/*
        // demonstrate how to load local asset using "file:resources/"
        Image bannerImage = new Image("file:resources/readme-banner.png");
        ImageView banner = new ImageView(bannerImage);
        banner.setPreserveRatio(true);
        banner.setFitWidth(640);

        // some labels to display information
        Label notice = new Label("Modify the starter code to suit your needs.");

        // setup scene
        root.getChildren().addAll(banner, notice, svHolder);
*/
        beautifyApp();

        HBox.setHgrow(newsButton1, Priority.ALWAYS);
        HBox.setHgrow(compareButton, Priority.ALWAYS);
        HBox.setHgrow(newsButton2, Priority.ALWAYS);

        newsButton1.setMaxWidth(Double.MAX_VALUE);
        compareButton.setMaxWidth(Double.MAX_VALUE);
        newsButton2.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().addAll(titleLayer, topLayer, svHolder, bottomLayer);

        titleLayer.getChildren().add(titleLabel);

        topLayer.getChildren().add(instructLabel);

//        rightsBox.getChildren().add(rightsLabel);

        svHolder.getChildren().addAll(stockView1, separator, stockView2);

        bottomLayer.getChildren().addAll(newsButton1, compareButton, newsButton2);

        scene = new Scene(root);

        // setup stage
        stage.setTitle("ApiApp!");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();

    } // start

    /** {@inheritDoc} */
    @Override
    public void init() {
        System.out.println("init called...");
        sv2Changes();
        setUpButtons();
    }

    /**
     * Private helper method that accounts for the necessary changes between the first
     * {@code StockViewer} and second {@code StockerViewer}.
     */
    private void sv2Changes() {
        stockView2.setSearchLabel("Search for Stock 2:");
    }

    /**
     * Private helper method to setup the EventHandler's for each button.
     */
    private void setUpButtons() {
        EventHandler<ActionEvent> compareHandler = (ActionEvent e) -> {
            try {
                stockView1.chartHttpConnect();
                stockView2.chartHttpConnect();
            } catch (NullPointerException npe) {
                alertError(npe);
            }
        };
        compareButton.setOnAction(compareHandler);

        EventHandler<ActionEvent> news1Handler = (ActionEvent e) -> {
            try {
                stockView1.newsHttpConnect();
            } catch (NullPointerException npe) {
                alertError(npe);
            }
        };
        newsButton1.setOnAction(news1Handler);

        EventHandler<ActionEvent> news2Handler = (ActionEvent e) -> {
            try {
                stockView2.newsHttpConnect();
            } catch (NullPointerException npe) {
                alertError(npe);
            }
        };
        newsButton2.setOnAction(news2Handler);
    }

    /**
     * Private helper method to present an error alert to the user.
     *
     * @param cause the cause of the error.
     */
    private void alertError(Throwable cause) {
        String errorMsg = cause.toString();
        TextArea text = new TextArea(errorMsg);
        text.setEditable(false);
        Alert alert = new Alert(AlertType.ERROR);
        alert.getDialogPane().setContent(text);
        alert.setResizable(true);
        alert.showAndWait();
    }

    /**
     * Private helper method to beautify the app using CSS styles.
     */
    private void beautifyApp() {
        titleLayer.setAlignment(Pos.CENTER);
        titleLabel.setStyle(
            "-fx-font-size: 30px;" +
            "-fx-font-family: Arial;" +
            "-fx-font-weight: bold;" +
            "-fx-font-style: italic;");

        compareButton.setStyle("-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: white; " +
            "-fx-border-width: 1px; " +
            "-fx-cursor: hand;");

        compareButton.setOnMouseEntered(e -> compareButton.setStyle(
            "-fx-background-color: #4aa3df; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: white; " +
            "-fx-border-width: 1px; " +
            "-fx-cursor: hand;"));

        compareButton.setOnMouseExited(e -> compareButton.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: white; " +
            "-fx-border-width: 1px; " +
            "-fx-cursor: hand;"));

        beautifyApp2();
    }

    /**
     * Private helper method to beautify even more of the app.
     */
    private void beautifyApp2() {

        newsButton1.setStyle("-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: white; " +
            "-fx-border-width: 1px; " +
            "-fx-cursor: hand;");

        newsButton1.setOnMouseEntered(e -> newsButton1.setStyle(
            "-fx-background-color: #4aa3df; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: white; " +
            "-fx-border-width: 1px; " +
            "-fx-cursor: hand;"));

        newsButton1.setOnMouseExited(e -> newsButton1.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: white; " +
            "-fx-border-width: 1px; " +
            "-fx-cursor: hand;"));
        newsButton2.setStyle("-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: white; " +
            "-fx-border-width: 1px; " +
            "-fx-cursor: hand;");

        newsButton2.setOnMouseEntered(e -> newsButton2.setStyle(
            "-fx-background-color: #4aa3df; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: white; " +
            "-fx-border-width: 1px; " +
            "-fx-cursor: hand;"));

        newsButton2.setOnMouseExited(e -> newsButton2.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: white; " +
            "-fx-border-width: 1px; " +
            "-fx-cursor: hand;"));
    }

} // ApiApp
