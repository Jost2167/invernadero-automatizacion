package com.jost.invernadero.automatizacion.entity;


import jakarta.persistence.Column;

import jakarta.persistence.Entity;

import jakarta.persistence.EnumType;

import jakarta.persistence.Enumerated;

import jakarta.persistence.FetchType;

import jakarta.persistence.GeneratedValue;

import jakarta.persistence.GenerationType;

import jakarta.persistence.Id;

import jakarta.persistence.JoinColumn;

import jakarta.persistence.ManyToOne;

import jakarta.persistence.Table;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Getter;

import lombok.NoArgsConstructor;

import lombok.Setter;


@Entity
@Table(name = "greenhouse_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GreenhouseAlert {



    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;



    @Column(nullable = false, length = 140)

    private String title;



    @Enumerated(EnumType.STRING)

    @Column(nullable = false)

    private GreenhouseAlertSeverity severity;



    @Column(nullable = false, length = 255)

    private String message;



    @Column(nullable = false)

    private LocalDateTime detectedAt;



    @Column(nullable = false)

    private Boolean resolved;




    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "greenhouse_id")


    private Greenhouse greenhouse;




    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "sensor_id")


    private Sensor sensor;



}
