package com.jost.invernadero.automatizacion.repository;

import com.jost.invernadero.automatizacion.entity.PestInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PestInspectionRepository extends JpaRepository<PestInspection, Long> {
}
