/*
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
*/


package com.cms902;

import com.cms902.manager.TrackManager;
import com.cms902.model.Scenario;
import com.cms902.simulation.SimulationEngine;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class CMS902Application extends Application {

    private SimulationEngine simulationEngine;

    @Override
    public void start(Stage stage) {
        simulationEngine = new SimulationEngine(Scenario.PEACETIME);
        TrackManager trackManager = new TrackManager();

        simulationEngine.addListener(trackManager);

        trackManager.addListener(tracks -> {
            System.out.println("=== TrackManager picture updated: " + tracks.size() + " tracks ===");
            tracks.forEach(System.out::println);
        });

        simulationEngine.start();

        Label label = new Label("CMS 902 Online.");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 400, 300);
        stage.setTitle("CMS 902");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (simulationEngine != null) {
            simulationEngine.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}