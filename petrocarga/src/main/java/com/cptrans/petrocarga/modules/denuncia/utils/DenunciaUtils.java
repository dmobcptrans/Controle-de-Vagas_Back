package com.cptrans.petrocarga.modules.denuncia.utils;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.modules.auth.exceptions.AuthExceptions;
import com.cptrans.petrocarga.modules.denuncia.exceptions.DenunciaExceptions;


@Component
public class DenunciaUtils {
    /**
     * Valida se uma denúncia pode ser criada com base nos dados informados.
     * 
     * @param novaDenuncia Denúncia a ser validada.
     * @throws IllegalArgumentException Se a denúncia não puder ser criada com base nos dados informados.
     */
    public static void validarCriacaoDenuncia(StatusReservaEnum statusReserva, UUID criadorReservaId, UUID motoristaReservaId, UUID criadorDenunciaId) {
        if (!statusReserva.equals(StatusReservaEnum.RESERVADA) && !statusReserva.equals(StatusReservaEnum.ATIVA)) throw new DenunciaExceptions.ReservaStatusInvalidException();

        if (!criadorReservaId.equals(criadorDenunciaId) && motoristaReservaId.equals(criadorDenunciaId)) throw new AuthExceptions.UsuarioNaoAutorizadoException();
    }

}