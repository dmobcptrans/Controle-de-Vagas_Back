package com.cptrans.petrocarga.modules.enderecoVaga.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.modules.enderecoVaga.entity.EnderecoVaga;

@Repository
public interface EnderecoVagaRepository extends JpaRepository<EnderecoVaga, UUID>{
    public Optional<EnderecoVaga> findByLogradouroIgnoreCase(String logradouro);
    @Query("""
        SELECT ev FROM EnderecoVaga ev
        WHERE LOWER(ev.logradouro) LIKE LOWER(CONCAT('%', :logradouro, '%'))
    """)
    public List<EnderecoVaga> findByLogradouro(String logradouro);
}
