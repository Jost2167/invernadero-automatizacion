CREATE TABLE fertilization_events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    applied_at TIMESTAMP NOT NULL,
    fertilizer_name VARCHAR(120) NOT NULL,
    dose NUMERIC(10,2) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    notes VARCHAR(255),
    crop_cycle_id BIGINT,
    CONSTRAINT fk_fertilization_events_crop_cycle FOREIGN KEY (crop_cycle_id) REFERENCES crop_cycles(id),
    CONSTRAINT chk_fertilization_events_unit CHECK (unit IN ('GRAMS','KILOGRAMS','MILLILITERS','LITERS'))
);
