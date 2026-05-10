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
@Table(name = "maintenance_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceTask {



    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;



    @Column(nullable = false, length = 140)

    private String title;



    @Column(nullable = true, length = 255)

    private String description;



    @Column(nullable = false)

    private LocalDateTime scheduledAt;



    @Column(nullable = true)

    private LocalDateTime completedAt;



    @Enumerated(EnumType.STRING)

    @Column(nullable = false)

    private MaintenanceTaskStatus status;




    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "greenhouse_id")


    private Greenhouse greenhouse;



}
