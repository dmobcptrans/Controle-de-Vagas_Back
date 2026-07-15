package com.cptrans.petrocarga.modules.denuncia.dto.request;

import java.util.List;
import java.util.UUID;

import com.cptrans.petrocarga.enums.StatusDenunciaEnum;
import com.cptrans.petrocarga.enums.TipoDenunciaEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class DenunciaFiltrosRequestDTO {
    private UUID id;
    private UUID vagaId;
    private UUID reservaId;
    private UUID criadoPorId;
    private String criadoPorNome;
    private String criadoPorTelefone;
    private List<StatusDenunciaEnum> listaStatus; 
    private List<TipoDenunciaEnum> listaTipos; 
}
