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
@Table(name = "pest_inspections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PestInspection {



    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;



    @Column(nullable = false)

    private LocalDateTime inspectedAt;



    @Column(nullable = false, length = 120)

    private String pestType;



    @Enumerated(EnumType.STRING)

    @Column(nullable = false)

    private PestInspectionSeverity severity;



    @Column(nullable = true, precision = 10, scale = 2)

    private BigDecimal affectedAreaSquareMeters;



    @Column(nullable = false)

    private Boolean treatmentApplied;




    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "crop_cycle_id")


    private CropCycle cropCycle;



}
