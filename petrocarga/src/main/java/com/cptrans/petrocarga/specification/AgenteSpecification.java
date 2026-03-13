package com.cptrans.petrocarga.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.cptrans.petrocarga.dto.AgenteFiltrosDTO;
import com.cptrans.petrocarga.models.Agente;

import jakarta.persistence.criteria.Predicate;

public class AgenteSpecification {
    /**
     * Cria uma Specification para filtrar agentes com base nos filtros passados.
     *
     * Os filtros são: nome, telefone, email, ativo e matricula.
     * Se nenhum filtro for passado, então retorna uma Specification que não filtra nada.
     *
     * @param filtros o objeto com os filtros para a busca
     * @return a Specification que filtra os agentes com base nos filtros passados
    */
     public static Specification<Agente> filtrar(
        AgenteFiltrosDTO filtros
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

            if (filtros.email() != null) {
                predicates.add(
                    cb.equal(root.get("usuario").get("email"), filtros.email())
                );
            }

            if (filtros.ativo() != null) {
                predicates.add(
                    cb.equal(root.get("usuario").get("ativo") , filtros.ativo())
                );
            }

            if (filtros.matricula() != null) {
                predicates.add(
                    cb.equal(root.get("matricula"), filtros.matricula())
                );
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
