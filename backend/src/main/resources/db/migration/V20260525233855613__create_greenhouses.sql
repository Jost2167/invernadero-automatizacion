CREATE TABLE greenhouses (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    area_square_meters NUMERIC(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    location_id BIGINT,
    CONSTRAINT fk_greenhouses_location FOREIGN KEY (location_id) REFERENCES locations(id),
    CONSTRAINT chk_greenhouses_status CHECK (status IN ('ACTIVE','INACTIVE','MAINTENANCE'))
);
