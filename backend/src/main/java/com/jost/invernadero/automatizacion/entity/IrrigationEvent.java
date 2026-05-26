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

import java.math.BigDecimal;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Getter;

import lombok.NoArgsConstructor;

import lombok.Setter;


@Entity
@Table(name = "irrigation_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrrigationEvent {



    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;



    @Column(nullable = false)

    private LocalDateTime startedAt;



    @Column(nullable = true)

    private LocalDateTime endedAt;



    @Column(nullable = false, precision = 10, scale = 2)

    private BigDecimal waterLiters;



    @Enumerated(EnumType.STRING)

    @Column(nullable = false)

    private IrrigationEventMethod method;



    @Column(nullable = true, length = 255)

    private String notes;




    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "greenhouse_id")


    private Greenhouse greenhouse;



}
