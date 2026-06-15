package com.cptrans.petrocarga.modules.notificacao.dto.request;

import com.cptrans.petrocarga.enums.TipoNotificacaoEnum;
import com.cptrans.petrocarga.modules.notificacao.entity.Notificacao;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class NotificacaoRequestDTO {
    @NotNull
    @Size(min = 3, max = 120, message = "Titulo deve ter entre 3 e 120 caracteres.")
    public String titulo;

    @NotNull
    @Size(min = 3, max = 1000, message = "Mensagem deve ter entre 3 e 1000 caracteres.")
    public String mensagem;

    @NotNull
    public TipoNotificacaoEnum tipo;

    
    public NotificacaoRequestDTO() {
    }

    public NotificacaoRequestDTO(String titulo, String mensagem) {
        this.titulo = titulo;
        this.mensagem = mensagem;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public Notificacao toEntity(){
        return new Notificacao(titulo, mensagem, tipo);
    }
}
