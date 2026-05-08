package com.cms902.ui;

import com.cms902.manager.TrackManager;
import com.cms902.model.Track;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

/**
 *
 * Draws the current set of tracks on a Canvas, redrawing from scratch
 * each time the TrackManager reports an update.
 */
public class RadarDisplay implements TrackManager.TrackPictureListener {

    private final Canvas canvas;
    private final GraphicsContext gc;

    // Center of the operating area.
    private static final double CENTER_LATITUDE = 50.0;
    private static final double CENTER_LONGITUDE = -35.0;

    // How many degrees of lat/lon the radar covers from center to edge.
    // The operating area spans 4° lat and 10° lon, so 2.5° from center fits both.
    private static final double DEGREES_FROM_CENTER_TO_EDGE = 2.5;

    /**
     * Builds a radar display of the given size.
     *
     * @param width canvas width in pixels
     * @param height canvas height in pixels
     */
    public RadarDisplay(double width, double height) {
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();
    }

    /**
     * Returns the underlying Canvas so it can be added to a JavaFX scene.
     */
    public Canvas getCanvas() {
        return canvas;
    }

    @Override
    public void onTrackPictureUpdated(List<Track> tracks) {
        drawBackground();
        drawTracks(tracks);
    }

    /**
     * Draws the static radar background: black fill, range rings, and crosshairs.
     * Called every time the display is redrawn before the tracks are put on top.
     */
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
     *
     * @param track the track to locate
     * @return a 2-element array [x, y] in canvas pixels
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
     * Loops through all tracks drawing them using a color and shape based
     * on its classification.
     *
     * @param tracks the current tactical picture
     */
    private void drawTracks(List<Track> tracks) {
        for (Track track : tracks) {
            double[] pixel = trackToPixel(track);
            drawTrackSymbol(track, pixel[0], pixel[1]);
        }
    }

    /**
     * Draws a single track's symbol at the given pixel position. Color and
     * shape are determined by the track's classification.
     */
    private void drawTrackSymbol(Track track, double x, double y) {
        double size = 10;
        Color color = colorFor(track.getClassification());
        gc.setFill(color);
        gc.fillText(track.getDesignation(), x + size, y + size / 2);
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