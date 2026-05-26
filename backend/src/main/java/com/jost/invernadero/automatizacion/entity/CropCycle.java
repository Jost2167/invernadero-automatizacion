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

import java.time.LocalDate;

import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Getter;

import lombok.NoArgsConstructor;

import lombok.Setter;


@Entity
@Table(name = "crop_cycles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropCycle {



    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;



    @Column(nullable = false, length = 120)

    private String cropName;



    @Column(nullable = true, length = 120)

    private String variety;



    @Column(nullable = false)

    private LocalDate startedAt;



    @Column(nullable = true)

    private LocalDate expectedHarvestAt;



    @Enumerated(EnumType.STRING)

    @Column(nullable = false)

    private CropCycleStatus status;




    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "greenhouse_id")


    private Greenhouse greenhouse;



}
