CREATE TABLE climate_readings (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    recorded_at TIMESTAMP NOT NULL,
    temperature_celsius NUMERIC(5,2) NOT NULL,
    humidity_percent NUMERIC(5,2) NOT NULL,
    co2_ppm INTEGER,
    light_lux INTEGER,
    sensor_id BIGINT,
    greenhouse_id BIGINT,
    CONSTRAINT fk_climate_readings_sensor FOREIGN KEY (sensor_id) REFERENCES sensors(id),
    CONSTRAINT fk_climate_readings_greenhouse FOREIGN KEY (greenhouse_id) REFERENCES greenhouses(id)
);
