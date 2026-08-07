package com.cptrans.petrocarga.modules.motorista.dto.response;

import java.time.LocalDate;
import java.util.UUID;

import com.cptrans.petrocarga.enums.TipoCnhEnum;
import com.cptrans.petrocarga.modules.usuario.dto.response.UsuarioSimplificadoResponseDTO;
import com.cptrans.petrocarga.shared.utils.StringUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MotoristaResumoResponseDTO {
    private UUID id;
    private UsuarioSimplificadoResponseDTO usuario;
    private TipoCnhEnum tipoCnh;
    private String numeroCnh;
    private LocalDate dataValidadeCnh;
    private UUID empresaId;
    private String empresaCnpj;
    private String empresaRazaoSocial;

    public void setNumeroCnh(String numeroCnh) {
        this.numeroCnh = numeroCnh;
    }

    public void setUsuario(UsuarioSimplificadoResponseDTO usuario) {
        this.usuario = usuario;
    }

    public void formatarDados() {
        this.empresaCnpj = StringUtils.formatarCnpj(this.empresaCnpj);
    }
}