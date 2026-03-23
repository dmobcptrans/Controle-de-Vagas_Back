package com.cptrans.petrocarga.shared.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.application.usecase.EmpresaService;
import com.cptrans.petrocarga.application.usecase.MotoristaService;
import com.cptrans.petrocarga.domain.entities.Denuncia;
import com.cptrans.petrocarga.domain.entities.Empresa;
import com.cptrans.petrocarga.domain.entities.Motorista;
import com.cptrans.petrocarga.domain.enums.PermissaoEnum;
import com.cptrans.petrocarga.domain.enums.StatusReservaEnum;

@Component
public class DenunciaUtils {

    @Autowired
    private static EmpresaService empresaService;
    @Autowired
    private static MotoristaService motoristaService;

    /**
     * Valida se uma denúncia pode ser criada com base nos dados informados.
     * 
     * @param novaDenuncia Denúncia a ser validada.
     * @throws IllegalArgumentException Se a denúncia não puder ser criada com base nos dados informados.
     */
    public static void validarCriacaoDenuncia(Denuncia novaDenuncia) {
        
        if (!novaDenuncia.getReserva().getStatus().equals(StatusReservaEnum.RESERVADA) && !novaDenuncia.getReserva().getStatus().equals(StatusReservaEnum.ATIVA)) throw new IllegalArgumentException("Reserva nao pode ser diferente de 'reservada' ou 'ativa'.");

        if (!novaDenuncia.getReserva().getCriadoPor().equals(novaDenuncia.getCriadoPor())){
            if (!novaDenuncia.getReserva().getCriadoPor().getPermissao().equals(PermissaoEnum.EMPRESA) || !novaDenuncia.getCriadoPor().getPermissao().equals(PermissaoEnum.MOTORISTA) ) throw new IllegalArgumentException("Usuário nao pode denunciar uma reserva de outro usuário.");

            Motorista motorista = motoristaService.findByUsuarioId(novaDenuncia.getCriadoPor().getId());
            Empresa empresa = empresaService.findByUsuarioId(novaDenuncia.getReserva().getCriadoPor().getId());

            if(!motorista.getEmpresa().getId().equals(empresa.getId())) {
                throw new IllegalArgumentException("Usuário nao pode denunciar uma reserva de outro usuário.");
            }
        }
    }
}
