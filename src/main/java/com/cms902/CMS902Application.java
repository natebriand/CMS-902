package com.cms902;

import com.cms902.manager.TrackManager;
import com.cms902.model.Scenario;
import com.cms902.simulation.SimulationEngine;
import com.cms902.ui.RadarDisplay;
import com.cms902.ui.MainWindow;
import com.cms902.persistence.TrackHistoryRepository;


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

        String jdbcUrl = System.getenv().getOrDefault("CMS902_DB_URL", "jdbc:mariadb://localhost:3307/cms902");
        String dbUser = System.getenv().getOrDefault("CMS902_DB_USER", "root");
        String dbPassword = System.getenv().getOrDefault("CMS902_DB_PASSWORD", "");

        if (!dbPassword.isEmpty()) {
            TrackHistoryRepository historyRepository = new TrackHistoryRepository(jdbcUrl, dbUser, dbPassword);
            trackManager.addListener(historyRepository);
        } else {
            System.out.println("Database password not set — track history persistence disabled. " +
                    "Set CMS902_DB_PASSWORD to enable.");
        }

        trackManager.addListener(tracks -> Platform.runLater(() -> radarDisplay.onTrackPictureUpdated(tracks)));

        simulationEngine.start();

        MainWindow mainWindow = new MainWindow(radarDisplay, trackManager, simulationEngine);
        Scene scene = mainWindow.buildScene(900, 700);
        stage.setTitle("CMS 902");
        stage.setScene(scene);
        stage.show();

        // Trigger the first draw so the radar background appears immediately
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