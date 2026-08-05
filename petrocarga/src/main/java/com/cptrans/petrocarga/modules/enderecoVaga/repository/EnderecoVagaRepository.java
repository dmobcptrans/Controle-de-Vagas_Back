package com.cptrans.petrocarga.modules.enderecoVaga.repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.modules.enderecoVaga.entity.EnderecoVaga;

@Repository
public interface EnderecoVagaRepository extends JpaRepository<EnderecoVaga, UUID>{
    public Optional<EnderecoVaga> findByCodigoPmpIgnoreCase(String codigoPmp);
    public Optional<EnderecoVaga> findByLogradouro(String logradouro);

    @Query("SELECT DISTINCT ev.codigoPmp FROM EnderecoVaga ev")
    public Set<String> findAllCodigosPmp();
}
