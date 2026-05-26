package com.jost.invernadero.automatizacion.repository;

import com.jost.invernadero.automatizacion.entity.GreenhouseAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GreenhouseAlertRepository extends JpaRepository<GreenhouseAlert, Long> {
}
