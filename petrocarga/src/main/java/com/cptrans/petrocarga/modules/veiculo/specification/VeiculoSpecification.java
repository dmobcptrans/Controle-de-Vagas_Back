package com.cptrans.petrocarga.modules.veiculo.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.cptrans.petrocarga.modules.veiculo.dto.request.VeiculoFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;

import jakarta.persistence.criteria.Predicate;

public class VeiculoSpecification {
    /**
     * Cria uma Specification para filtrar veiculos com base nos filtros passados.
     *
     * Os filtros são: placa, marca, modelo, tipo e usuarioId.
     * Se nenhum filtro for passado, então retorna uma Specification que não filtra nada.
     *
     * @param filtros o objeto com os filtros para a busca
     * @return a Specification que filtra os agentes com base nos filtros passados
    */
     public static Specification<Veiculo> filtrar(
        VeiculoFiltrosRequestDTO filtros
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();


            if (filtros.getPlaca() != null) {
                predicates.add(
                    cb.like(cb.upper(root.get("placa")), "%" + filtros.getPlaca().trim().toUpperCase() + "%")
                );
            }

            if (filtros.getMarca() != null) {
                predicates.add(
                    cb.like(cb.upper(root.get("marca")), "%" + filtros.getMarca().trim().toUpperCase() + "%")

                );
            }

            if (filtros.getModelo() != null) {
                predicates.add(
                    cb.like(cb.upper(root.get("modelo")), "%" + filtros.getModelo().trim().toUpperCase() + "%")

                );
            }

            if (filtros.getTipo() != null) {
                predicates.add(
                    cb.equal(root.get("tipo"), filtros.getTipo())
                );
            }

            if (filtros.getTelefoneUsuario() != null) {
                predicates.add(
                    cb.equal(root.get("usuario").get("telefoneHash"), filtros.getTelefoneUsuario())
                );
            }

            if (filtros.getCpfProprietario() != null) {
                predicates.add(
                    cb.equal(root.get("cpfProprietarioHash"), filtros.getCpfProprietario())
                );
            }

            if (filtros.getCnpjProprietario() != null) {
                predicates.add(
                    cb.equal(root.get("cnpjProprietario"), filtros.getCnpjProprietario())
                );
            }

            if (filtros.getUsuarioId() != null) {
                predicates.add(
                    cb.equal(root.get("usuario").get("id"), filtros.getUsuarioId())
                );
            }

            if (filtros.getAtivo() != null) {
                predicates.add(
                    cb.equal(root.get("ativo"), filtros.getAtivo())
                );
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}