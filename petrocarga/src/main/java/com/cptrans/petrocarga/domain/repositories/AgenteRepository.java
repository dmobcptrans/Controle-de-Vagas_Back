package com.cptrans.petrocarga.domain.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.domain.entities.Agente;
import com.cptrans.petrocarga.domain.entities.Usuario;

@Repository
public interface AgenteRepository extends JpaRepository<Agente, UUID>, JpaSpecificationExecutor<Agente> {
    public Optional<Agente> findByUsuario(Usuario usuario);
    public Optional<Agente> findByUsuarioAndUsuarioAtivo(Usuario usuario, Boolean ativo);
    public Optional<Agente> findByIdAndUsuarioAtivo(UUID agenteId, Boolean ativo);
    public Optional<Agente> findByMatricula(String matricula);
    public Boolean existsByMatricula(String matricula);
}
