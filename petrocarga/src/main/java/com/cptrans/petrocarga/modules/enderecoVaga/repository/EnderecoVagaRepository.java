package com.cptrans.petrocarga.modules.enderecoVaga.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.modules.enderecoVaga.entity.EnderecoVaga;

@Repository
public interface EnderecoVagaRepository extends JpaRepository<EnderecoVaga, UUID>{
    public Optional<EnderecoVaga> findByCodigoPmp(String codigoPmp);
    public Optional<EnderecoVaga> findByLogradouro(String logradouro);
}
