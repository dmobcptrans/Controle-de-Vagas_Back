package com.cptrans.petrocarga.modules.operacaoVaga.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.modules.operacaoVaga.entity.OperacaoVaga;

@Repository
public interface OperacaoVagaRepository extends JpaRepository<OperacaoVaga, UUID>{
    
}
