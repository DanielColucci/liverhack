package com.liverpool.liverhack.repositories;

import com.liverpool.liverhack.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // <-- Buena práctica
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Usuario findByUsernameAndPassword(String username, String password);

}