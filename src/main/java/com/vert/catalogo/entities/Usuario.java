package com.vert.catalogo.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Usuarios")
@NoArgsConstructor
@Getter @Setter
public class Usuario {

    @Id
    private Integer id;
    private String username;
    private String password;
}
