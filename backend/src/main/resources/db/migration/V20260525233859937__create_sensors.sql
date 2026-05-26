CREATE TABLE sensors (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    last_reading_at TIMESTAMP,
    battery_level NUMERIC(5,2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    location_id BIGINT,
    CONSTRAINT fk_sensors_location FOREIGN KEY (location_id) REFERENCES locations(id),
    CONSTRAINT chk_sensors_type CHECK (type IN ('TEMPERATURE','HUMIDITY','LIGHT','SOIL_MOISTURE'))
);
