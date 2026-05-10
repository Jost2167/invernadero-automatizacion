CREATE TABLE crop_cycles (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    crop_name VARCHAR(120) NOT NULL,
    variety VARCHAR(120),
    started_at DATE NOT NULL,
    expected_harvest_at DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'PLANNED',
    greenhouse_id BIGINT,
    CONSTRAINT fk_crop_cycles_greenhouse FOREIGN KEY (greenhouse_id) REFERENCES greenhouses(id),
    CONSTRAINT chk_crop_cycles_status CHECK (status IN ('PLANNED','GROWING','HARVESTED','CANCELLED'))
);
