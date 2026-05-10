CREATE TABLE track_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    snapshot_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    track_id VARCHAR(36) NOT NULL,
    designation VARCHAR(20) NOT NULL,
    track_type VARCHAR(20) NOT NULL,
    classification VARCHAR(20) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    heading DOUBLE NOT NULL,
    speed DOUBLE NOT NULL
);