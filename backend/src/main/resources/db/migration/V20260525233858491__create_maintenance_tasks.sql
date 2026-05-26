CREATE TABLE maintenance_tasks (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(140) NOT NULL,
    description VARCHAR(255),
    scheduled_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    greenhouse_id BIGINT,
    CONSTRAINT fk_maintenance_tasks_greenhouse FOREIGN KEY (greenhouse_id) REFERENCES greenhouses(id),
    CONSTRAINT chk_maintenance_tasks_status CHECK (status IN ('PENDING','IN_PROGRESS','COMPLETED','CANCELLED'))
);
