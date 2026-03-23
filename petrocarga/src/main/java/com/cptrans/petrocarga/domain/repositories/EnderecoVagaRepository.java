package com.cptrans.petrocarga.domain.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.domain.entities.EnderecoVaga;

@Repository
public interface EnderecoVagaRepository extends JpaRepository<EnderecoVaga, UUID>{
    public Optional<EnderecoVaga> findByCodigoPmp(String codigoPmp);
    public Optional<EnderecoVaga> findByLogradouro(String logradouro);
}
