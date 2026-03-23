package com.cptrans.petrocarga.domain.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.domain.entities.Denuncia;
import com.cptrans.petrocarga.domain.enums.StatusDenunciaEnum;
import com.cptrans.petrocarga.domain.enums.TipoDenunciaEnum;

@Repository
public interface DenunciaRepository extends JpaRepository<Denuncia, UUID> {
    public boolean existsByReservaId(UUID reservaId);
    public List<Denuncia> findByCriadoPorIdAndStatus(UUID usuarioId, StatusDenunciaEnum status);
    public List<Denuncia> findByCriadoPorId(UUID usuarioId);
    public List<Denuncia> findByVagaId(UUID vagaId);
    public List<Denuncia> findByStatusIn(List<StatusDenunciaEnum> status);
    public List<Denuncia> findByTipoIn(List<TipoDenunciaEnum> tipo);
    public List<Denuncia> findByVagaIdAndStatusInAndTipoIn(UUID vagaId, List<StatusDenunciaEnum> status, List<TipoDenunciaEnum> tipo);

}
