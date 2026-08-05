package com.cptrans.petrocarga.modules.usuario.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.shared.utils.StringUtils;
import com.cptrans.petrocarga.shared.utils.Utils;

import jakarta.persistence.criteria.Predicate;

public class UsuarioSpecification {
    public static Specification<Usuario> filtrar(UsuarioFiltrosRequestDTO filtros){
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtros.getNome() != null && !filtros.getNome().trim().isEmpty()){
                predicates.add(
                    cb.like(Utils.createUnaccentExpression(cb, cb.lower(root.get("nome"))), "%" + StringUtils.removerAcentos(filtros.getNome().trim().toLowerCase()) + "%")
                );
            }

            if (filtros.getEmail() != null && !filtros.getEmail().trim().isEmpty()){
                predicates.add(
                  cb.equal(root.get("emailHash"), filtros.getEmail().trim())  
                );
            }

            if (filtros.getTelefone() != null && !filtros.getTelefone().trim().isEmpty()){
                predicates.add(
                  cb.equal(root.get("telefoneHash"), filtros.getTelefone().trim())  
                );
            }

            if (filtros.getListaPermissoes() != null && !filtros.getListaPermissoes().isEmpty()){
                predicates.add(
                  root.get("permissao").in(filtros.getListaPermissoes())
                );
            }

            if (filtros.getAtivo() != null){
                predicates.add(
                  cb.equal(root.get("ativo"), filtros.getAtivo())  
                );
            }

            return cb.and(predicates.toArray(Predicate[]::new));

        };
    }
}