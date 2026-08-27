package com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.entity;

import java.time.OffsetDateTime;
import java.util.UUID;


import com.cptrans.petrocarga.enums.StatusConviteMotoristaEmpresaEnum;
import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.exceptions.ConviteMotoristaEmpresaExceptions;
import com.cptrans.petrocarga.modules.cripto.HashService;
import com.cptrans.petrocarga.modules.empresa.entity.Empresa;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "convite_motorista_empresa")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConviteMotoristaEmpresa {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // pode ser nulo, caso o motorista não exista ainda
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "motorista_id", nullable = true) 
    private Motorista motorista;

    @Column(name = "motorista_email_hash", nullable = false)
    private String motoristaEmailHash;

    @Column(name = "motorista_email_cripto", nullable = false)
    private String motoristaEmailCripto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusConviteMotoristaEmpresaEnum status = StatusConviteMotoristaEmpresaEnum.PENDENTE;

    //pode ser null para invalidar o token após o uso
    @Column(name = "token_hash", nullable = true, unique = true)
    private String tokenHash;

    @Column(name = "criado_em", columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
    private final OffsetDateTime criadoEm = DateUtils.agora();

    @Column(name = "respondido_em", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime respondidoEm;

    @Column(name = "valido_ate", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private final OffsetDateTime validoAte = criadoEm.plusDays(7);

    @Column(name = "cripto_version", nullable = false)
    private Integer criptoVersion = 1;

    public ConviteMotoristaEmpresa(Motorista motorista, Empresa empresa, String motoristaEmailHash, String motoristaEmailCripto, Integer criptoVersion) {
        this.motorista = motorista;
        this.empresa = empresa;
        this.motoristaEmailHash = motoristaEmailHash;
        this.motoristaEmailCripto = motoristaEmailCripto;
        this.criptoVersion = criptoVersion;
    }

    public ConviteMotoristaEmpresa(Empresa empresa, String motoristaEmailHash, String motoristaEmailCripto, Integer criptoVersion) {
        this.empresa = empresa;
        this.motoristaEmailHash = motoristaEmailHash;
        this.motoristaEmailCripto = motoristaEmailCripto;
        this.criptoVersion = criptoVersion;
    }

    public boolean isValido() {
        return this.status.equals(StatusConviteMotoristaEmpresaEnum.PENDENTE) && this.validoAte.toInstant().isAfter(DateUtils.agora().toInstant()) && this.tokenHash != null;
    }

    public void aceitar() {
        if (!isValido()) throw new ConviteMotoristaEmpresaExceptions.ConviteInvalidoException();
        this.status = StatusConviteMotoristaEmpresaEnum.ACEITO;
        this.respondidoEm = DateUtils.agora();
        this.tokenHash = null;
    }

    public void recusar() {
        if (!isValido()) throw new ConviteMotoristaEmpresaExceptions.ConviteInvalidoException();
        this.status = StatusConviteMotoristaEmpresaEnum.RECUSADO;
        this.respondidoEm = DateUtils.agora();
        this.tokenHash = null;
    }

    public String gerarToken(HashService hashService) {
        String token = HashService.gerarToken();
        this.tokenHash = hashService.hash(token);
        return token;
    }

    public boolean tokenIsValido(HashService hashService, String token) {
        if (!isValido()) return false;
        if (hashService == null || token == null || token.isBlank()) return false;
        return this.tokenHash.equals(hashService.hash(token));
    }

    public void vincularMotorista(Motorista motorista) {
        this.motorista = motorista;
    }

}