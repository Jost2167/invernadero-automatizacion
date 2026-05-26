package com.jost.invernadero.automatizacion.repository;

import com.jost.invernadero.automatizacion.entity.CropCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CropCycleRepository extends JpaRepository<CropCycle, Long> {
}
