-- Create pothole_reports table
CREATE TABLE pothole_reports (
    id BIGSERIAL PRIMARY KEY,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    location GEOMETRY(POINT, 4326),
    severity VARCHAR(20),
    description TEXT,
    device_id VARCHAR(100),
    verified BOOLEAN DEFAULT FALSE,
    verification_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create spatial index for fast queries
CREATE INDEX idx_pothole_location ON pothole_reports USING GIST(location);
CREATE INDEX idx_pothole_verified ON pothole_reports(verified);
CREATE INDEX idx_pothole_created_at ON pothole_reports(created_at);

-- Create blockchain_records table
CREATE TABLE blockchain_records (
    id BIGSERIAL PRIMARY KEY,
    pothole_id INT NOT NULL REFERENCES pothole_reports(id),
    transaction_hash VARCHAR(255),
    block_number INT,
    contract_address VARCHAR(255),
    stored_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create verification_events table
CREATE TABLE verification_events (
    id BIGSERIAL PRIMARY KEY,
    pothole_id INT NOT NULL REFERENCES pothole_reports(id),
    device_id VARCHAR(100),
    verified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);