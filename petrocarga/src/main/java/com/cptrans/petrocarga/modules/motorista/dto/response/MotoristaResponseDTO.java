package com.cptrans.petrocarga.modules.motorista.dto.response;

import java.time.LocalDate;
import java.util.UUID;

import com.cptrans.petrocarga.enums.TipoCnhEnum;
import com.cptrans.petrocarga.modules.usuario.dto.response.UsuarioResponseDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MotoristaResponseDTO {
    private UUID id;
    private UsuarioResponseDTO usuario;
    private TipoCnhEnum tipoCnh;
    private String numeroCnh;
    private LocalDate dataValidadeCnh;
    private UUID empresaId;
    private String empresaCnpj;
    private String empresaRazaoSocial;

    public void setNumeroCnh(String numeroCnh) {
        this.numeroCnh = numeroCnh;
    }

    public void setUsuario(UsuarioResponseDTO usuario) {
        this.usuario = usuario;
    }
}