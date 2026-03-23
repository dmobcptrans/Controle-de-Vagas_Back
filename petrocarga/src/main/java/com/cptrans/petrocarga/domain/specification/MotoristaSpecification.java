package com.cptrans.petrocarga.domain.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.cptrans.petrocarga.application.dto.MotoristaFiltrosDTO;
import com.cptrans.petrocarga.domain.entities.Motorista;

import jakarta.persistence.criteria.Predicate;

public class MotoristaSpecification {
    /**
     * Cria uma Specification para filtrar motoristas com base nos filtros passados.
     *
     * Os filtros são: nome, telefone, cnh, ativo.
     * Se nenhum filtro for passado, então retorna uma Specification que não filtra nada.
     *
     * @param filtros o objeto com os filtros para a busca
     * @return a Specification que filtra os motoristas com base nos filtros passados
     */
     public static Specification<Motorista> filtrar(
        MotoristaFiltrosDTO filtros
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filtros.nome() != null) {
                predicates.add(
                    cb.like(cb.lower(root.get("usuario").get("nome")), "%" + filtros.nome().trim().toLowerCase() + "%")
                );
            }

            if (filtros.telefone() != null) {
                predicates.add(
                    cb.equal(root.get("usuario").get("telefone"), filtros.telefone())
                );
            }

            if (filtros.cnh() != null) {
                predicates.add(
                    cb.equal(root.get("numero_cnh"), filtros.cnh())
                );
            }

            if (filtros.ativo() != null) {
                predicates.add(
                    cb.equal(root.get("usuario").get("ativo") , filtros.ativo())
                );
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
