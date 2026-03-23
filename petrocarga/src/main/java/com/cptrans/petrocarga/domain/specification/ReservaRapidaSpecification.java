package com.cptrans.petrocarga.domain.specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.cptrans.petrocarga.domain.entities.ReservaRapida;
import com.cptrans.petrocarga.domain.enums.StatusReservaEnum;

import jakarta.persistence.criteria.Predicate;


public class ReservaRapidaSpecification {
    /**
     * Cria uma Specification para filtrar reservas rápidas com base nos filtros passados.
     * Os filtros são: usuarioId, vagaId, placaVeiculo, data e listaStatus.
     * Se nenhum filtro for passado, então retorna uma Specification que não filtra nada.
     *
     * @param usuarioId o id do usuário para buscar as reservas rápidas
     * @param vagaId o id da vaga para filtrar as reservas
     * @param placaVeiculo a placa do veículo para filtrar as reservas
     * @param data a data da reserva para filtrar as reservas
     * @param listaStatus a lista de status para filtrar as reservas
     * @return a Specification que filtra as reservas r pidas com base nos filtros passados
     * 
     */
    public static Specification<ReservaRapida> filtrar(
        UUID usuarioId,
        UUID vagaId,
        String placaVeiculo,
        LocalDate data,
        List<StatusReservaEnum> listaStatus
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                cb.equal(root.get("agente").get("usuario").get("id"), usuarioId)
            );

            if (vagaId != null) {
                predicates.add(
                    cb.equal(root.get("vaga").get("id"), vagaId)
                );
            }

            if (placaVeiculo != null) {
                predicates.add(
                    cb.equal(root.get("placa"), placaVeiculo.toUpperCase())
                );
            }

            if (data != null) {
                predicates.add(
                    cb.equal(
                        cb.function("DATE", LocalDate.class, root.get("inicio")),
                        data
                    )
                );
            }

            if (listaStatus != null && !listaStatus.isEmpty()) {
                predicates.add(
                    root.get("status").in(listaStatus)
                );
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
