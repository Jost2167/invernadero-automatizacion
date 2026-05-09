package com.jost.invernadero.automatizacion.repository;

import com.jost.invernadero.automatizacion.entity.FertilizationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FertilizationEventRepository extends JpaRepository<FertilizationEvent, Long> {
}
