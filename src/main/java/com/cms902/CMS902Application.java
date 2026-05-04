package com.cms902;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class CMS902Application extends Application {

    @Override
    public void start(Stage stage) {
        Label label = new Label("CMS 902 Online.");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 400, 300);
        stage.setTitle("CMS 902");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}