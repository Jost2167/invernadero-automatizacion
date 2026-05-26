package com.jost.invernadero.automatizacion.repository;

import com.jost.invernadero.automatizacion.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {
}
