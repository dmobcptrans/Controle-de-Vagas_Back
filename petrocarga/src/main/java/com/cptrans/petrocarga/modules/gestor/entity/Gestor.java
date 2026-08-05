package com.cptrans.petrocarga.modules.gestor.entity;

import java.util.UUID;

import com.cptrans.petrocarga.modules.usuario.entity.Usuario;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gestor")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class Gestor {
    @Id
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "cpf_hash", nullable = false)
    private String cpfHash;

    @Column(name = "cpf_cripto", unique = true, nullable = false)
    private String cpfCripto;

    @Column(name = "cpf_last5", nullable = false)
    private String cpfLast5;

    public Gestor(Usuario usuario, String cpfHash, String cpfCripto, String cpfLast5) {
        this.usuario = usuario;
        this.cpfHash = cpfHash;
        this.cpfCripto = cpfCripto;
        this.cpfLast5 = cpfLast5;
    }


    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    public void setCpfHash(String cpfHash) {
        this.cpfHash = cpfHash;
    }
    public void setCpfCripto(String cpfCripto) {
        this.cpfCripto = cpfCripto;
    }
    public void setCpfLast5(String cpfLast5) {
        this.cpfLast5 = cpfLast5;
    }
}