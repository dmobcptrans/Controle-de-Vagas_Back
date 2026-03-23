package com.cptrans.petrocarga.application.usecase;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.application.dto.ReservaResponseDTO;
import com.cptrans.petrocarga.domain.entities.Reserva;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Service
public class ReservaSearchService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Busca reservas com joins já carregados (fetch) por um termo de busca.
     * O termo é comparado com motorista.usuario.nome, criadoPor.nome, veiculo.placa e cidadeOrigem.
     */
    public List<ReservaResponseDTO> searchByQuery(String q) {
        if (q == null || q.trim().isEmpty()) {
            return List.of();
        }

        String p = "%" + q.toLowerCase() + "%";

        String jpql = "SELECT distinct r FROM Reserva r "
                + "LEFT JOIN FETCH r.vaga v "
                + "LEFT JOIN FETCH v.endereco e "
                + "LEFT JOIN FETCH r.motorista m "
                + "LEFT JOIN FETCH m.usuario mu "
                + "LEFT JOIN FETCH r.veiculo ve "
                + "LEFT JOIN FETCH ve.usuario veu "
                + "LEFT JOIN FETCH r.criadoPor cp "
                + "WHERE lower(mu.nome) LIKE :p OR lower(cp.nome) LIKE :p OR lower(ve.placa) LIKE :p OR lower(r.cidadeOrigem) LIKE :p";

        TypedQuery<Reserva> query = em.createQuery(jpql, Reserva.class);
        query.setParameter("p", p);

        List<Reserva> results = query.getResultList();
        return results.stream().map(Reserva::toResponseDTO).collect(Collectors.toList());
    }
}
