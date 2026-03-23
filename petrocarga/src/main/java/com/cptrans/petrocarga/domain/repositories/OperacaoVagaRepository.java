package com.cptrans.petrocarga.domain.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.domain.entities.OperacaoVaga;

@Repository
public interface OperacaoVagaRepository extends JpaRepository<OperacaoVaga, UUID>{
    
}
