package com.cms902.ui;

import com.cms902.manager.TrackManager;
import com.cms902.model.Scenario;
import com.cms902.simulation.SimulationEngine;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

/**
 * Top-level window layout for the CMS 902 application.
 *
 * Holds the radar in the center, a control panel on the left, and a status
 * bar at the bottom. The control panel lets the user change scenario and
 * start or stop the simulation.
 */
public class MainWindow {

    private final BorderPane root;
    private final RadarDisplay radarDisplay;
    private final TrackManager trackManager;
    private SimulationEngine simulationEngine;
    private ComboBox<Scenario> scenarioPicker;
    private Label statusLabel;
    private boolean simulationRunning = true;

    /**
     * Builds the window layout around the given radar, track manager, and
     * initial simulation engine.
     */
    public MainWindow(RadarDisplay radarDisplay, TrackManager trackManager, SimulationEngine simulationEngine) {
        this.radarDisplay = radarDisplay;
        this.trackManager = trackManager;
        this.simulationEngine = simulationEngine;

        root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.web("#1a1a1a"), null, null)));

        // Place each piece in its region. Order matters here: buildStatusBar() reads scenarioPicker,
        // which is created inside buildControlPanel(), so the control panel must be built first.
        root.setCenter(radarDisplay.getCanvas());
        root.setLeft(buildControlPanel());
        root.setBottom(buildStatusBar());
    }

    /**
     * Builds the control panel on the left side of the window.
     * Runs once at startup. Each button gets a click handler attached that runs
     * later, only when the user clicks.
     */
    private VBox buildControlPanel() {
        // VBox stacks children vertically. The 10 is the gap in pixels between each child.
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(180);

        Label heading = new Label("Controls");
        heading.setTextFill(Color.LIGHTGRAY);

        Label scenarioLabel = new Label("Scenario");
        scenarioLabel.setTextFill(Color.LIGHTGRAY);

        // ComboBox is JavaFX's dropdown menu. Items are all four Scenario enum values.
        scenarioPicker = new ComboBox<>();
        scenarioPicker.getItems().addAll(Scenario.values());
        scenarioPicker.setValue(Scenario.PEACETIME);

        // By default the dropdown would show the raw enum names ("PEACETIME").
        // The converter tells it to show the friendly displayName ("Peacetime Patrol") instead.
        // toString: how to display a Scenario as text.
        // fromString: how to turn typed text back into a Scenario. Unused here, so null.
        scenarioPicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(Scenario scenario) {
                return scenario == null ? "" : scenario.displayName;
            }
            @Override
            public Scenario fromString(String string) {
                return null;
            }
        });

        // Each button's setOnAction attaches a lambda. The lambda is stored
        // on the button and runs later every time the user clicks.
        Button applyButton = new Button("Apply Scenario");
        applyButton.setOnAction(e -> {
            applyScenario();
            simulationRunning = true;
            updateStatus(trackManager.getTracks().size());
        });

        Button startButton = new Button("Start");
        startButton.setOnAction(e -> {
            simulationEngine.start();
            simulationRunning = true;
            updateStatus(trackManager.getTracks().size());
        });

        Button stopButton = new Button("Stop");
        stopButton.setOnAction(e -> {
            simulationEngine.stop();
            simulationRunning = false;
            updateStatus(trackManager.getTracks().size());
        });

        // Add everything to the VBox in display order from top to bottom.
        panel.getChildren().addAll(heading, scenarioLabel, scenarioPicker, applyButton, startButton, stopButton);
        return panel;
    }

    /**
     * Stops the current simulation and replaces it with a new one based on
     * the currently selected scenario. The new simulation is wired to the
     * existing TrackManager and starts immediately.
     */
    private void applyScenario() {
        Scenario chosen = scenarioPicker.getValue();
        if (chosen == null) return;

        // The old simulation is discarded and its background thread is shut down by stop().
        simulationEngine.stop();
        simulationEngine = new SimulationEngine(chosen);
        simulationEngine.addListener(trackManager);
        simulationEngine.start();

        // Trigger an immediate redraw of the tracks
        Platform.runLater(() -> radarDisplay.onTrackPictureUpdated(trackManager.getTracks()));
    }

    /**
     * Builds the status bar at the bottom of the window.
     */
    private Label buildStatusBar() {
        statusLabel = new Label();
        statusLabel.setPadding(new Insets(5, 10, 5, 10));
        statusLabel.setTextFill(Color.LIGHTGRAY);

        // Show the initial state right away, before the first tick arrives.
        updateStatus(trackManager.getTracks().size());

        // Subscribe to TrackManager so the status updates on every tick.
        trackManager.addListener(tracks -> Platform.runLater(() -> updateStatus(tracks.size())));

        return statusLabel;
    }

    /**
     * Refreshes the status bar with current scenario, run state, and track count.
     */
    private void updateStatus(int trackCount) {
        String state = simulationRunning ? "RUNNING" : "STOPPED";
        String scenario = scenarioPicker.getValue().displayName;

        statusLabel.setText(String.format("Scenario: %s    |    State: %s    |    Tracks: %d",
                scenario, state, trackCount));
    }

    /**
     * Wraps the layout in a Scene ready to be shown by the JavaFX Stage.
     */
    public Scene buildScene(double width, double height) {
        return new Scene(root, width, height);
    }
}