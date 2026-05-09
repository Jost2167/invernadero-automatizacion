package com.jost.invernadero.automatizacion.entity;


import jakarta.persistence.Column;

import jakarta.persistence.Entity;

import jakarta.persistence.FetchType;

import jakarta.persistence.GeneratedValue;

import jakarta.persistence.GenerationType;

import jakarta.persistence.Id;

import jakarta.persistence.JoinColumn;

import jakarta.persistence.ManyToOne;

import jakarta.persistence.Table;

import java.math.BigDecimal;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Getter;

import lombok.NoArgsConstructor;

import lombok.Setter;


@Entity
@Table(name = "climate_readings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClimateReading {



    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;



    @Column(nullable = false)

    private LocalDateTime recordedAt;



    @Column(nullable = false, precision = 5, scale = 2)

    private BigDecimal temperatureCelsius;



    @Column(nullable = false, precision = 5, scale = 2)

    private BigDecimal humidityPercent;



    @Column(nullable = true)

    private Integer co2Ppm;



    @Column(nullable = true)

    private Integer lightLux;




    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "sensor_id")


    private Sensor sensor;




    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "greenhouse_id")


    private Greenhouse greenhouse;



}
