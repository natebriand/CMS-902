package com.cms902.ui;

import com.cms902.manager.TrackManager;
import com.cms902.model.Scenario;
import com.cms902.model.Track;
import com.cms902.simulation.SimulationEngine;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Top-level window: radar in the center, controls on the left, status bar on the bottom.
 * */
public class MainWindow {

    private final BorderPane root;
    private final RadarDisplay radarDisplay;
    private final TrackManager trackManager;
    private SimulationEngine simulationEngine;
    private ComboBox<Scenario> scenarioPicker;
    private Label statusLabel;
    private boolean simulationRunning = true;

    // UI state for track visibility/details.
    private final Set<String> visibleTrackIds = new HashSet<>();
    private final Set<String> knownTrackIds = new HashSet<>();
    private VBox trackSelectionBox;
    private CheckBox showTrackDetailsCheckBox;


    public MainWindow(RadarDisplay radarDisplay, TrackManager trackManager, SimulationEngine simulationEngine) {
        this.radarDisplay = radarDisplay;
        this.trackManager = trackManager;
        this.simulationEngine = simulationEngine;

        root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.web("#1a1a1a"), null, null)));

        // Order matters: buildStatusBar reads scenarioPicker, which buildControlPanel creates.
        root.setCenter(radarDisplay.getCanvas());
        root.setLeft(buildControlPanel());
        root.setBottom(buildStatusBar());

        // All tracks are visible when the window is first created.
        initializeTrackSelection(trackManager.getTracks());
    }

    private VBox buildControlPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(210);

        Label heading = new Label("Controls");
        heading.setTextFill(Color.LIGHTGRAY);

        Label scenarioLabel = new Label("Scenario");
        scenarioLabel.setTextFill(Color.LIGHTGRAY);

        Label legendLabel = new Label("Legend");
        legendLabel.setTextFill(Color.LIGHTGRAY);

        // ComboBox is JavaFX's dropdown menu. Items are all four Scenario enum values.
        scenarioPicker = new ComboBox<>();
        scenarioPicker.getItems().addAll(Scenario.values());
        scenarioPicker.setValue(Scenario.PEACETIME);

        // Show the human-readable displayName instead of the raw enum constant.
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

        showTrackDetailsCheckBox = new CheckBox("Show Track Details");
        showTrackDetailsCheckBox.setTextFill(Color.LIGHTGRAY);
        showTrackDetailsCheckBox.setSelected(false);
        showTrackDetailsCheckBox.setOnAction(e ->
                radarDisplay.setShowTrackDetails(showTrackDetailsCheckBox.isSelected())
        );

        Label tracksLabel = new Label("Visible Tracks");
        tracksLabel.setTextFill(Color.LIGHTGRAY);

        trackSelectionBox = new VBox(5);
        ScrollPane trackScrollPane = new ScrollPane(trackSelectionBox);
        trackScrollPane.setFitToWidth(true);
        trackScrollPane.setPrefHeight(180);
        trackScrollPane.setMaxHeight(180);
        trackScrollPane.setStyle("-fx-background: #1a1a1a; -fx-background-color: #1a1a1a;");

        panel.getChildren().addAll(
                heading,
                scenarioLabel,
                scenarioPicker,
                applyButton,
                startButton,
                stopButton,
                legendLabel,
                buildLegend(),
                showTrackDetailsCheckBox,
                tracksLabel,
                trackScrollPane
        );
        return panel;
    }

    private VBox buildLegend() {
        VBox legend = new VBox(8);

        legend.getChildren().addAll(
                createLegendItem("Friendly", Color.DEEPSKYBLUE, "circle"),
                createLegendItem("Hostile", Color.RED, "diamond"),
                createLegendItem("Neutral", Color.LIMEGREEN, "square"),
                createLegendItem("Suspect", Color.YELLOW, "diamond"),
                createLegendItem("Unknown", Color.YELLOW, "outline")
        );

        return legend;
    }

    private HBox createLegendItem(String text, Color color, String symbolType) {
        HBox item = new HBox(10);
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        javafx.scene.Node symbol;

        switch (symbolType) {
            case "circle" -> {
                Circle circle = new Circle(6);
                circle.setFill(color);
                symbol = circle;
            }

            case "diamond" -> {
                Polygon diamond = new Polygon(0.0, -7.0, 7.0, 0.0, 0.0, 7.0, -7.0, 0.0);
                diamond.setFill(color);
                symbol = diamond;
            }

            case "square" -> {
                Rectangle square = new Rectangle(12, 12);
                square.setFill(color);
                symbol = square;
            }

            case "outline" -> {
                Rectangle square = new Rectangle(12, 12);
                square.setFill(Color.TRANSPARENT);
                square.setStroke(color);
                square.setStrokeWidth(2);
                symbol = square;
            }

            default -> symbol = new Rectangle(12, 12, color);
        }

        Label label = new Label(text);
        label.setTextFill(Color.LIGHTGRAY);

        item.getChildren().addAll(symbol, label);

        return item;
    }

    /**
     * Creates the track visibility list. Every track starts selected.
     */
    private void initializeTrackSelection(List<Track> tracks) {
        visibleTrackIds.clear();
        knownTrackIds.clear();

        for (Track track : tracks) {
            visibleTrackIds.add(track.getDesignation());
            knownTrackIds.add(track.getDesignation());
        }

        rebuildTrackSelection(tracks);
        radarDisplay.setVisibleTrackIds(visibleTrackIds);
    }

    /**
     * Keeps the selection list synchronized with the current track picture while
     * preserving the user's selections between simulation ticks.
     */
    private void updateTrackSelection(List<Track> tracks) {
        Set<String> currentTrackIds = new HashSet<>();
        for (Track track : tracks) {
            currentTrackIds.add(track.getDesignation());
        }

        boolean trackSetChanged = !currentTrackIds.equals(knownTrackIds);
        if (trackSetChanged) {
            // Remove tracks that no longer exist and select newly created tracks.
            visibleTrackIds.retainAll(currentTrackIds);
            for (String trackId : currentTrackIds) {
                if (!knownTrackIds.contains(trackId)) {
                    visibleTrackIds.add(trackId);
                }
            }

            knownTrackIds.clear();
            knownTrackIds.addAll(currentTrackIds);
            rebuildTrackSelection(tracks);
        }

        radarDisplay.setVisibleTrackIds(visibleTrackIds);
    }

    private void rebuildTrackSelection(List<Track> tracks) {
        trackSelectionBox.getChildren().clear();

        for (Track track : tracks) {
            CheckBox checkBox = new CheckBox(track.getDesignation());
            checkBox.setTextFill(Color.LIGHTGRAY);
            checkBox.setSelected(visibleTrackIds.contains(track.getDesignation()));
            checkBox.setOnAction(e -> {
                if (checkBox.isSelected()) {
                    visibleTrackIds.add(track.getDesignation());
                } else {
                    visibleTrackIds.remove(track.getDesignation());
                }
                radarDisplay.setVisibleTrackIds(visibleTrackIds);
            });
            trackSelectionBox.getChildren().add(checkBox);
        }
    }

    /**
     * Stops the current simulation and replaces it with a new one based on
     * the currently selected scenario.
     */
    private void applyScenario() {
        Scenario chosen = scenarioPicker.getValue();
        if (chosen == null) return;

        simulationEngine.stop();
        simulationEngine = new SimulationEngine(chosen);
        simulationEngine.addListener(trackManager);

        // A new scenario creates a new track set, so start with every track visible.
        initializeTrackSelection(simulationEngine.getTracks());
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
        trackManager.addListener(tracks -> Platform.runLater(() -> {
            updateStatus(tracks.size());
            updateTrackSelection(tracks);
        }));

        return statusLabel;
    }

    /**
     * Refreshes the status bar with current scenario, run state, and track count.
     */
    private void updateStatus(int trackCount) {
        String state = simulationRunning ? "RUNNING" : "STOPPED";
        String scenario = simulationEngine.getScenario().displayName;

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
