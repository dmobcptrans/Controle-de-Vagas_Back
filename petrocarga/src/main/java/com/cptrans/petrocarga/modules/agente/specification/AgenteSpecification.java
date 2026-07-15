package com.cptrans.petrocarga.modules.agente.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.cptrans.petrocarga.modules.agente.dto.request.AgenteFiltrosDTO;
import com.cptrans.petrocarga.modules.agente.entity.Agente;
import com.cptrans.petrocarga.shared.utils.StringUtils;
import com.cptrans.petrocarga.shared.utils.Utils;

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
                    cb.like(Utils.createUnaccentExpression(cb, cb.lower(root.get("usuario").get("nome"))), "%" + StringUtils.normalize(filtros.nome().trim().toLowerCase()) + "%")
                );
            }

            if (filtros.ativo() != null) {
                predicates.add(
                    cb.equal(root.get("usuario").get("ativo") , filtros.ativo())
                );
            }

            if (filtros.matricula() != null) {
                predicates.add(
                    cb.like(root.get("matricula"), "%" + filtros.matricula().trim() + "%")
                );
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}