package com.cptrans.petrocarga.modules.gestor.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.cptrans.petrocarga.modules.gestor.dto.request.GestorFiltrosDTO;
import com.cptrans.petrocarga.modules.gestor.entity.Gestor;
import com.cptrans.petrocarga.shared.utils.StringUtils;
import com.cptrans.petrocarga.shared.utils.Utils;

import jakarta.persistence.criteria.Predicate;

public class GestorSpecification {
    /**
     * Cria uma Specification para filtrar usu  rios com base nos filtros passados.
     *
     * Os filtros são: nome, telefone, email e ativo.
     * Se nenhum filtro for passado, então retorna uma Specification que não filtra nada.
     *
     * @param filtros o objeto com os filtros para a busca
     * @return a Specification que filtra os gestores com base nos filtros passados
     */
     public static Specification<Gestor> filtrar(
        GestorFiltrosDTO filtros
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filtros.getId() != null) {
                predicates.add(
                    cb.equal(root.get("id"), filtros.getId())
                );
            }

            if (filtros.getNome() != null) {
                predicates.add(
                    cb.like(cb.lower(Utils.createUnaccentExpression(cb, root.get("usuario").get("nome"))), "%" + StringUtils.normalize(filtros.getNome().trim().toLowerCase())+ "%")
                );
            }

            if (filtros.getTelefone() != null) {
                predicates.add(
                    cb.equal(root.get("usuario").get("telefoneHash"), filtros.getTelefone())
                );
            }

            if (filtros.getEmail() != null) {
                predicates.add(
                    cb.equal(root.get("usuario").get("emailHash"), filtros.getEmail().trim().toLowerCase())
                );
            }

            if (filtros.getAtivo() != null) {
                predicates.add(
                    cb.equal(root.get("usuario").get("ativo"), filtros.getAtivo())
                );
            }

            if (filtros.getCpf() != null) {
                predicates.add(
                    cb.equal(root.get("cpfHash"), filtros.getCpf())
                );
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}