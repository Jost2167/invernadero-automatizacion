CREATE TABLE pest_inspections (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    inspected_at TIMESTAMP NOT NULL,
    pest_type VARCHAR(120) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    affected_area_square_meters NUMERIC(10,2),
    treatment_applied BOOLEAN NOT NULL DEFAULT FALSE,
    crop_cycle_id BIGINT,
    CONSTRAINT fk_pest_inspections_crop_cycle FOREIGN KEY (crop_cycle_id) REFERENCES crop_cycles(id),
    CONSTRAINT chk_pest_inspections_severity CHECK (severity IN ('LOW','MEDIUM','HIGH','CRITICAL'))
);
