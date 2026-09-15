package com.vert.catalogo.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.vert.catalogo.entities.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsername(String username);
}
