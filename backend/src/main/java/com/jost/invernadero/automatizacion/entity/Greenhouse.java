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

import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Getter;

import lombok.NoArgsConstructor;

import lombok.Setter;


@Entity
@Table(name = "greenhouses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Greenhouse {



    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;



    @Column(nullable = false, unique = true, length = 40)

    private String code;



    @Column(nullable = false, length = 120)

    private String name;



    @Column(nullable = false, precision = 10, scale = 2)

    private BigDecimal areaSquareMeters;



    @Enumerated(EnumType.STRING)

    @Column(nullable = false)

    private GreenhouseStatus status;



    @Column(nullable = false)

    private Boolean active;




    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "location_id")


    private Location location;



}
