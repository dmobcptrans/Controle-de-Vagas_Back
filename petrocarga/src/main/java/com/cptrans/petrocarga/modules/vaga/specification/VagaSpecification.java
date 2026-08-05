package com.cptrans.petrocarga.modules.vaga.specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.cptrans.petrocarga.enums.StatusVagaEnum;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.entity.DisponibilidadeVaga;
import com.cptrans.petrocarga.modules.vaga.dto.request.VagaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
import com.cptrans.petrocarga.shared.utils.DateUtils;
import com.cptrans.petrocarga.shared.utils.StringUtils;
import com.cptrans.petrocarga.shared.utils.Utils;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public class VagaSpecification {
    public static Specification<Vaga> filtrar(VagaFiltrosRequestDTO filtros) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtros.getCodigoPmp() != null && !filtros.getCodigoPmp().trim().isEmpty()) {
                predicates.add(
                    cb.like(cb.lower(root.get("endereco").get("codigoPmp")), "%" + filtros.getCodigoPmp().trim().toLowerCase() + "%")
                );
            }

            if (filtros.getLogradouro() != null && !filtros.getLogradouro().trim().isEmpty()) {
                predicates.add(
                    cb.like(Utils.createUnaccentExpression(cb, cb.lower(root.get("endereco").get("logradouro"))), "%" + StringUtils.removerAcentos(filtros.getLogradouro().trim().toLowerCase()) + "%")
                );
            }

            if (filtros.getBairro() != null && !filtros.getBairro().trim().isEmpty()) {
                predicates.add(
                    cb.like(Utils.createUnaccentExpression(cb, cb.lower(root.get("endereco").get("bairro"))), "%" + StringUtils.removerAcentos(filtros.getBairro().trim().toLowerCase()) + "%")
                );
            }

            if (filtros.getArea() != null) {
                predicates.add(cb.equal(root.get("area"), filtros.getArea()));
            }

            if (filtros.getTipo() != null) {
                predicates.add(cb.equal(root.get("tipo"), filtros.getTipo()));
            }

            if (filtros.getStatus() != null) {
                if (filtros.getStatus() == StatusVagaEnum.DISPONIVEL) {
                    Join<Vaga, DisponibilidadeVaga> disponibilidade = root.join("disponibilidades", JoinType.INNER);

                    OffsetDateTime agora = DateUtils.agora();

                    predicates.add(cb.lessThanOrEqualTo(
                            disponibilidade.get("inicio"), agora));

                    predicates.add(cb.greaterThan(
                            disponibilidade.get("fim"), agora));
                }

                predicates.add(cb.equal(root.get("status"), filtros.getStatus()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}