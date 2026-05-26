CREATE TABLE greenhouse_alerts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(140) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    message VARCHAR(255) NOT NULL,
    detected_at TIMESTAMP NOT NULL,
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    greenhouse_id BIGINT,
    sensor_id BIGINT,
    CONSTRAINT fk_greenhouse_alerts_greenhouse FOREIGN KEY (greenhouse_id) REFERENCES greenhouses(id),
    CONSTRAINT fk_greenhouse_alerts_sensor FOREIGN KEY (sensor_id) REFERENCES sensors(id),
    CONSTRAINT chk_greenhouse_alerts_severity CHECK (severity IN ('INFO','WARNING','CRITICAL'))
);
