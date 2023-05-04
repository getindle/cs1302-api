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
    // declare variables
    Stage stage;
    Scene scene;
    VBox root;
    HBox titleLayer;
    Label titleLabel;
    HBox topLayer;
    Label instructLabel;
    HBox svHolder;
    HBox rightsLayer;
    Label rightsLabel1;
    Label rightsLabel2;
    // declare custom components
    StockViewer stockView1;
    StockViewer stockView2;
    Separator separator; // to separate the custom components
    // declare buttons and hbox for buttons
    HBox bottomLayer;
    Button newsButton1;
    Button newsButton2;
    Button compareButton;

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new VBox();
        // construct app skeleton
        titleLayer = new HBox();
        titleLabel = new Label("StockMatch");
        topLayer = new HBox();
        topLayer.setPadding(new Insets(3, 0, 5, 0));
        instructLabel = new Label
        ("Directions: Enter 2 valid stock tickers, then compare them or see relevant news. ");
        rightsLabel1 = new Label("Only 5 API calls allowed per minute");
        rightsLabel1.setStyle("-fx-underline: true;");
        rightsLabel2 = new Label(" (two successful \"compare\" clicks).");
        // construct custom components
        svHolder = new HBox();
        stockView1 = new StockViewer();
        stockView2 = new StockViewer();
        separator = new Separator();
        // construct buttons
        bottomLayer = new HBox();
        newsButton1 = new Button("Stock 1 News");
        newsButton2 = new Button("Stock 2 News");
        compareButton = new Button("Compare");
        newsButton1.setDisable(true);
        newsButton2.setDisable(true);
    } // ApiApp

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

        this.stage = stage;
        // make buttons look fancy
        beautifyApp();
        HBox.setHgrow(newsButton1, Priority.ALWAYS);
        HBox.setHgrow(compareButton, Priority.ALWAYS);
        HBox.setHgrow(newsButton2, Priority.ALWAYS);
        newsButton1.setMaxWidth(Double.MAX_VALUE);
        compareButton.setMaxWidth(Double.MAX_VALUE);
        newsButton2.setMaxWidth(Double.MAX_VALUE);

        // add components to scene
        root.getChildren().addAll(titleLayer, topLayer, svHolder, bottomLayer);
        titleLayer.getChildren().add(titleLabel);
        topLayer.getChildren().addAll(instructLabel, rightsLabel1, rightsLabel2);
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
        // call methods to setup app functionality
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
        // set up compare button
        EventHandler<ActionEvent> compareHandler = (ActionEvent e) -> {
            try {
                stockView1.chartHttpConnect();
                String button1Name = stockView1.buttonName + " News";
                newsButton1.setText(button1Name);
                stockView2.chartHttpConnect();
                String button2Name = stockView2.buttonName + " News";
                newsButton2.setText(button2Name);
                newsButton1.setDisable(false);
                newsButton2.setDisable(false);
            } catch (NullPointerException npe) {
                newsButton1.setDisable(true);
                newsButton2.setDisable(true);
                alertError(npe); // alert error if not enough data found
            }
        };
        compareButton.setOnAction(compareHandler);
        // set up news 1 button
        EventHandler<ActionEvent> news1Handler = (ActionEvent e) -> {
            try {
                stockView1.newsHttpConnect();
                newsButton1.setDisable(true);
            } catch (NullPointerException npe) {
                newsButton1.setDisable(true);
                newsButton2.setDisable(true);
                alertError(npe); // alert error if not enough articles found
            }
        };
        newsButton1.setOnAction(news1Handler);
        // set up news 2 button
        EventHandler<ActionEvent> news2Handler = (ActionEvent e) -> {
            try {
                stockView2.newsHttpConnect();
                newsButton2.setDisable(true);
            } catch (NullPointerException npe) {
                newsButton1.setDisable(true);
                newsButton2.setDisable(true);
                alertError(npe); // alert error if not enough articles found
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
        // create pop-up alert error for user
        String errorMsg = cause.toString(); // message in the error
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
        titleLabel.setStyle( // style the "StockMatch" title
            "-fx-font-size: 30px;" +
            "-fx-font-family: Arial;" +
            "-fx-font-weight: bold;" +
            "-fx-font-style: italic;");
        // make compare button a modern blue with 90 degree corners
        compareButton.setStyle("-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: white; " +
            "-fx-border-width: 1px; " +
            "-fx-cursor: hand;");
        // if mouse is hovering on compare button, lighten button color & change mouse to hand
        compareButton.setOnMouseEntered(e -> compareButton.setStyle(
            "-fx-background-color: #4aa3df; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: white; " +
            "-fx-border-width: 1px; " +
            "-fx-cursor: hand;"));
        // if mouse leaves compare button, reset button style to before
        compareButton.setOnMouseExited(e -> compareButton.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: white; " +
            "-fx-border-width: 1px; " +
            "-fx-cursor: hand;"));
        beautifyApp2(); // call part 2 of beautify method
    }

    /**
     * Private helper method to beautify even more of the app.
     */
    private void beautifyApp2() {
        // make both news button's style the exact same as the compare button
        newsButton1.setStyle("-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: white; " +
            "-fx-border-width: 1px; " +
            "-fx-cursor: hand;");
        // when mouse is on stock news 1 button
        newsButton1.setOnMouseEntered(e -> newsButton1.setStyle(
            "-fx-background-color: #4aa3df; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: white; " +
            "-fx-border-width: 1px; " +
            "-fx-cursor: hand;"));
        // when mouse exits stock news 1 button
        newsButton1.setOnMouseExited(e -> newsButton1.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: white; " +
            "-fx-border-width: 1px; " +
            "-fx-cursor: hand;"));
        // set up style for stock news 2 button
        newsButton2.setStyle("-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: white; " +
            "-fx-border-width: 1px; " +
            "-fx-cursor: hand;");
        // when mouse hovers stock news 2 button
        newsButton2.setOnMouseEntered(e -> newsButton2.setStyle(
            "-fx-background-color: #4aa3df; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: white; " +
            "-fx-border-width: 1px; " +
            "-fx-cursor: hand;"));
        // when mouse exits stock news 2 button
        newsButton2.setOnMouseExited(e -> newsButton2.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: white; " +
            "-fx-border-width: 1px; " +
            "-fx-cursor: hand;"));
    }
} // ApiApp
