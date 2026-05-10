package com.cms902.persistence;

import com.cms902.manager.TrackManager;
import com.cms902.model.Track;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Persists each tick's track snapshot to MariaDB.
 */
public class TrackHistoryRepository implements TrackManager.TrackPictureListener {

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private static final String INSERT_SQL =
            "INSERT INTO track_snapshots " +
            "(track_id, designation, track_type, classification, latitude, longitude, heading, speed) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    public TrackHistoryRepository(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    /**
     * Called whenever the TrackManager publishes an update.
     */
    @Override
    public void onTrackPictureUpdated(List<Track> tracks) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {

            for (Track track : tracks) {
                stmt.setString(1, track.getTrackId());
                stmt.setString(2, track.getDesignation());
                stmt.setString(3, track.getTrackType().name());
                stmt.setString(4, track.getClassification().name());
                stmt.setDouble(5, track.getLatitude());
                stmt.setDouble(6, track.getLongitude());
                stmt.setDouble(7, track.getHeading());
                stmt.setDouble(8, track.getSpeed());

                stmt.addBatch();
            }

            stmt.executeBatch();

        } catch (SQLException e) {
            System.err.println("Failed to persist track snapshot: " + e.getMessage());
        }
    }
}