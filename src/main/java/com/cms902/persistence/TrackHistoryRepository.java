package com.cms902.persistence;

import com.cms902.manager.TrackManager;
import com.cms902.model.Track;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Saves track snapshots to a SQL database.
 *
 * Implements TrackPictureListener so it can be plugged into the existing
 * listener architecture.Every time TrackManager publishes an update this
 * class writes the current tracks to the database for potential future use.
 */
public class TrackHistoryRepository implements TrackManager.TrackPictureListener {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    /**
     * Builds a repository pointed at the given database.
     *
     * @param jdbcUrl JDBC connection URL
     * @param username database username
     * @param password database password
     */
    public TrackHistoryRepository(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    /**
     * Called whenever the TrackManager publishes an update. Inserts one
     * row per track into the snapshot table.
     */
    @Override
    public void onTrackPictureUpdated(List<Track> tracks) {
        // SQL template with ? placeholders
        String sql = "INSERT INTO track_snapshots " +
                "(track_id, designation, track_type, classification, latitude, longitude, heading, speed) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // Open a connection and a statement. Try-with-resources will close
        // them both when this block ends or if it throws an exception.
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Fill in the ? placeholders for each track.
            for (Track track : tracks) {
                stmt.setString(1, track.getTrackId());
                stmt.setString(2, track.getDesignation());
                stmt.setString(3, track.getTrackType().name());
                stmt.setString(4, track.getClassification().name());
                stmt.setDouble(5, track.getLatitude());
                stmt.setDouble(6, track.getLongitude());
                stmt.setDouble(7, track.getHeading());
                stmt.setDouble(8, track.getSpeed());

                // Queue this row to be inserted
                stmt.addBatch();
            }

            // Send all the queued rows to the database at once
            stmt.executeBatch();

        } catch (SQLException e) {
            System.err.println("Failed to persist track snapshot: " + e.getMessage());
        }
    }
}