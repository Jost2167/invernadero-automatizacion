package com.jost.invernadero.automatizacion.repository;

import com.jost.invernadero.automatizacion.entity.IrrigationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IrrigationEventRepository extends JpaRepository<IrrigationEvent, Long> {
}
