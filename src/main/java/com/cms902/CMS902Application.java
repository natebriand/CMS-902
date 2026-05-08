package com.cms902;

import com.cms902.manager.TrackManager;
import com.cms902.model.Scenario;
import com.cms902.simulation.SimulationEngine;
import com.cms902.ui.RadarDisplay;
import com.cms902.ui.MainWindow;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CMS902Application extends Application {

    private SimulationEngine simulationEngine;

    @Override
    public void start(Stage stage) {
        simulationEngine = new SimulationEngine(Scenario.PEACETIME);
        TrackManager trackManager = new TrackManager();
        RadarDisplay radarDisplay = new RadarDisplay(600, 600);

        simulationEngine.addListener(trackManager);
        trackManager.addListener(tracks -> Platform.runLater(() -> radarDisplay.onTrackPictureUpdated(tracks)));

        simulationEngine.start();

        MainWindow mainWindow = new MainWindow(radarDisplay, trackManager, simulationEngine);
        Scene scene = mainWindow.buildScene(900, 700);
        stage.setTitle("CMS 902");
        stage.setScene(scene);
        stage.show();

        // Trigger the first draw so the radar background appears immediately,
        // rather than waiting for the first 2 second tick.
        radarDisplay.onTrackPictureUpdated(trackManager.getTracks());
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