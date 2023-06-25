package com.dat.dateca.domain.professor;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Professor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;
    @Column(unique = true)
    private Long registrationNumber;
    String name;
    String phone;
    String email;
}
