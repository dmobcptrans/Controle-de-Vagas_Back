package com.cptrans.petrocarga.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.models.PushToken;

@Repository
public interface PushTokenRepository extends JpaRepository<PushToken, UUID> {
    List<PushToken> findByUsuarioId(UUID usuarioId);
    Optional<PushToken> findByToken(String token);
    Optional<PushToken> findByTokenAndUsuarioId(String token, UUID usuarioId);
    List<PushToken> findByUsuarioIdAndAtivo(UUID usuarioid, boolean ativo);
}
