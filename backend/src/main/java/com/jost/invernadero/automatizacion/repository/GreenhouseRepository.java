package com.jost.invernadero.automatizacion.repository;

import com.jost.invernadero.automatizacion.entity.Greenhouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GreenhouseRepository extends JpaRepository<Greenhouse, Long> {
}
