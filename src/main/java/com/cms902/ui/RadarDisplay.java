package com.cms902.ui;

import com.cms902.manager.TrackManager;
import com.cms902.model.Track;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Set;

/**
 * Canvas based radar display. Redraws from scratch on every TrackManager update.
 * */
public class RadarDisplay implements TrackManager.TrackPictureListener {

    private final Canvas canvas;
    private final GraphicsContext gc;

    private static final double CENTER_LATITUDE = 50.0;
    private static final double CENTER_LONGITUDE = -35.0;

    // 2.5° covers the 4° x 10° operating area in both axes.
    private static final double DEGREES_FROM_CENTER_TO_EDGE = 2.0;

    // Display state controlled by MainWindow.
    private volatile Set<String> visibleTrackIds = Set.of();
    private volatile boolean showTrackDetails = false;
    private volatile List<Track> currentTracks = List.of();


    public RadarDisplay(double width, double height) {
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();
    }


    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Sets which tracks are allowed to appear on the radar.
     */
    public void setVisibleTrackIds(Set<String> trackIds) {
        this.visibleTrackIds = Set.copyOf(trackIds);
        redraw();
    }

    /**
     * Enables or disables additional information beside each track.
     */
    public void setShowTrackDetails(boolean showTrackDetails) {
        this.showTrackDetails = showTrackDetails;
        redraw();
    }

    @Override
    public void onTrackPictureUpdated(List<Track> tracks) {
        this.currentTracks = List.copyOf(tracks);
        redraw();
    }

    private void redraw() {
        drawBackground();
        drawTracks(currentTracks);
    }

    private void drawBackground() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;
        double maxRadius = Math.min(width, height) / 2;

        // Black background.
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);

        // Range rings.
        gc.setStroke(Color.DARKGREEN);
        gc.setLineWidth(1);
        for (int i = 1; i <= 4; i++) {
            double radius = maxRadius * (i / 4.0);
            gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        }

        // Crosshairs
        gc.strokeLine(centerX, centerY - maxRadius, centerX, centerY + maxRadius);
        gc.strokeLine(centerX - maxRadius, centerY, centerX + maxRadius, centerY);
    }

    /**
     * Converts a track's lat/lon to a pixel position on the canvas.
     */
    private double[] trackToPixel(Track track) {
        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;
        double maxRadius = Math.min(canvas.getWidth(), canvas.getHeight()) / 2;

        double pixelsPerDegree = maxRadius / DEGREES_FROM_CENTER_TO_EDGE;

        double deltaLat = track.getLatitude() - CENTER_LATITUDE;
        double deltaLon = track.getLongitude() - CENTER_LONGITUDE;

        double x = centerX + (deltaLon * pixelsPerDegree);
        double y = centerY - (deltaLat * pixelsPerDegree); // minus because pixel y is flipped

        return new double[] { x, y };
    }

    /**
     * Loops through the current tracks and draws only the tracks selected by the user.
     */
    private void drawTracks(List<Track> tracks) {
        for (Track track : tracks) {
            if (!visibleTrackIds.contains(track.getDesignation())) {
                continue;
            }

            double[] pixel = trackToPixel(track);
            drawTrackSymbol(track, pixel[0], pixel[1]);
        }
    }

    /**
     * Draws a single track's symbol at the given pixel position.
     */
    private void drawTrackSymbol(Track track, double x, double y) {
        double size = 10;
        Color color = colorFor(track.getClassification());
        gc.setFill(color);

        String label = track.getDesignation();
        if (showTrackDetails) {
            label += " | " + track.getTrackType() + " | " + track.getClassification();
        }
        gc.fillText(label, x + size, y + size / 2);

        gc.setStroke(color);
        gc.setLineWidth(2);

        switch (track.getClassification()) {
            case FRIENDLY -> gc.fillOval(x - size / 2, y - size / 2, size, size);
            case HOSTILE -> drawDiamond(x, y, size);
            case NEUTRAL -> gc.fillRect(x - size / 2, y - size / 2, size, size);
            case SUSPECT -> drawDiamond(x, y, size);
            case UNKNOWN -> gc.strokeRect(x - size / 2, y - size / 2, size, size);
        }
    }

    /**
     * Returns the display color for a given classification.
     */
    private Color colorFor(Track.Classification classification) {
        return switch (classification) {
            case FRIENDLY -> Color.DEEPSKYBLUE;
            case HOSTILE -> Color.RED;
            case NEUTRAL -> Color.LIMEGREEN;
            case SUSPECT -> Color.YELLOW;
            case UNKNOWN -> Color.YELLOW;
        };
    }

    /**
     * Draws a filled diamond (rotated square) centered at the given position.
     */
    private void drawDiamond(double x, double y, double size) {
        double half = size / 2;
        double[] xPoints = { x, x + half, x, x - half };
        double[] yPoints = { y - half, y, y + half, y };
        gc.fillPolygon(xPoints, yPoints, 4);
    }
}