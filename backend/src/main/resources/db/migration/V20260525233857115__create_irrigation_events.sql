CREATE TABLE irrigation_events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP,
    water_liters NUMERIC(10,2) NOT NULL,
    method VARCHAR(50) NOT NULL,
    notes VARCHAR(255),
    greenhouse_id BIGINT,
    CONSTRAINT fk_irrigation_events_greenhouse FOREIGN KEY (greenhouse_id) REFERENCES greenhouses(id),
    CONSTRAINT chk_irrigation_events_method CHECK (method IN ('DRIP','SPRINKLER','MANUAL'))
);
