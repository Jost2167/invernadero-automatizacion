package com.jost.invernadero.automatizacion.repository;

import com.jost.invernadero.automatizacion.entity.ClimateReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClimateReadingRepository extends JpaRepository<ClimateReading, Long> {
}
