package com.early_reflections.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO this should also work without getClassloader??
        Font.loadFont(getClass().getClassLoader().getResource("fontawesome-webfont.ttf").toExternalForm(), 12);
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("BacktestUi.fxml"));
        primaryStage.setTitle("Backtester");
        primaryStage.setScene(new Scene(root, 1024, 800));
        primaryStage.setMaximized(true);
        primaryStage.getScene().getStylesheets().add("stylesheet.css");
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> System.exit(0));
    }

    public static void main(String[] args) {
        launch(args);
    }
}