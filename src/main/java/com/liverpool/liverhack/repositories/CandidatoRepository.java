package com.liverpool.liverhack.repositories;

import com.liverpool.liverhack.models.Candidato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CandidatoRepository extends JpaRepository<Candidato, Long> {

    Candidato findByNombre(String nombre);
    List<Candidato> findByStatusProceso(String statusProceso);

}