package com.cptrans.petrocarga.domain.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.cptrans.petrocarga.application.dto.GestorFiltrosDTO;
import com.cptrans.petrocarga.domain.entities.Usuario;
import com.cptrans.petrocarga.domain.enums.PermissaoEnum;

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
     public static Specification<Usuario> filtrar(
        GestorFiltrosDTO filtros
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

             predicates.add(
                    cb.equal(root.get("permissao"), PermissaoEnum.GESTOR)
                );

            if (filtros.nome() != null) {
                predicates.add(
                    cb.like(cb.lower(root.get("nome")), "%" + filtros.nome().trim().toLowerCase() + "%")
                );
            }

            if (filtros.telefone() != null) {
                predicates.add(
                    cb.equal(root.get("telefone"), filtros.telefone())
                );
            }

            if (filtros.email() != null) {
                predicates.add(
                    cb.equal(root.get("email"), filtros.email())
                );
            }

            if (filtros.ativo() != null) {
                predicates.add(
                    cb.equal(root.get("ativo"), filtros.ativo())
                );
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
