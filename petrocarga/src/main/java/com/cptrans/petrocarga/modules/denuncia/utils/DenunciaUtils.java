package com.cptrans.petrocarga.modules.denuncia.utils;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.enums.StatusDenunciaEnum;
import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.modules.auth.exceptions.AuthExceptions;
import com.cptrans.petrocarga.modules.denuncia.exceptions.DenunciaExceptions;
import com.cptrans.petrocarga.modules.denuncia.repository.DenunciaRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor 
public class DenunciaUtils {

    private final DenunciaRepository denunciaRepository;

    /**
     * Valida se uma denúncia pode ser criada com base nos dados informados.
     * 
     * @param novaDenuncia Denúncia a ser validada.
     * @throws IllegalArgumentException Se a denúncia não puder ser criada com base nos dados informados.
     */
    public static void validarCriacaoDenuncia(StatusReservaEnum statusReserva, UUID criadorReservaId, UUID motoristaReservaId, UUID criadorDenunciaId) {
        if (!statusReserva.equals(StatusReservaEnum.RESERVADA) && !statusReserva.equals(StatusReservaEnum.ATIVA)) throw new DenunciaExceptions.ReservaStatusInvalidException();

        if (!criadorReservaId.equals(criadorDenunciaId) && !motoristaReservaId.equals(criadorDenunciaId)) throw new AuthExceptions.UsuarioNaoAutorizadoException();
    }

    public boolean existeDenunciaAtivaNaVaga(UUID vagaId){
        List<StatusDenunciaEnum> statusDenuncia = List.of(StatusDenunciaEnum.ABERTA, StatusDenunciaEnum.EM_ANALISE);
        return denunciaRepository.existsByVagaIdAndStatusIn(vagaId, statusDenuncia);
    }

    @Transactional 
    public void excluirDenunciasDaVaga(UUID vagaId){
        Set<UUID> denuncias = denunciaRepository.findIdsByVagaId(vagaId);
        denunciaRepository.deleteAllById(denuncias);
    }

}