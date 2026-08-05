package com.cptrans.petrocarga.modules.motorista.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.validator.constraints.br.CPF;

import com.cptrans.petrocarga.enums.TipoCnhEnum;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioRequestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MotoristaRequestDTO {
    @Valid
    @NotNull(message = "O campo 'usuario' é obrigatório.")
    private UsuarioRequestDTO usuario;

    @NotNull(message="O campo 'cpf' é obrigatório.")
    @CPF(message="CPF inválido.")
    private String cpf;

    @NotNull(message = "O campo 'tipoCnh' é obrigatório.")
    private TipoCnhEnum tipoCnh;

    @NotNull(message = "O campo 'numeroCnh' é obrigatório.")
    @Pattern(
        regexp = "^\\d{9,11}$",
        message = "Número da CNH deve conter apenas números e ter entre 9 e 11 dígitos"
    )
    private String numeroCnh;

    @NotNull(message = "O campo 'dataValidadeCnh' é obrigatório.")
    @Future(message = "Data de validade da CNH está vencida ou vence hoje.")
    private LocalDate dataValidadeCnh;

    private UUID empresaId;
}