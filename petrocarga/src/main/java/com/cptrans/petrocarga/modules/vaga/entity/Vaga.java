package com.cptrans.petrocarga.modules.vaga.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import com.cptrans.petrocarga.enums.AreaVagaEnum;
import com.cptrans.petrocarga.enums.StatusVagaEnum;
import com.cptrans.petrocarga.enums.TipoVagaEnum;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.entity.DisponibilidadeVaga;
import com.cptrans.petrocarga.modules.enderecoVaga.entity.EnderecoVaga;
import com.cptrans.petrocarga.modules.operacaoVaga.entity.OperacaoVaga;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vaga")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Vaga {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endereco_id", nullable = false)
    @JsonManagedReference
    private EnderecoVaga endereco;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AreaVagaEnum area;

    @Column(name = "numero_endereco")
    private String numeroEndereco;

    @Column(name = "referencia_endereco")
    private String referenciaEndereco;

    @Column(name = "tipo_vaga", nullable=false)
    @Enumerated(EnumType.STRING)
    private TipoVagaEnum tipoVaga;
    
    @Column(name = "latitude_inicio")
    private Double latitudeInicio;

    @Column(name = "longitude_inicio")
    private Double longitudeInicio;

    @Column(name = "latitude_fim")
    private Double latitudeFim;

    @Column(name = "longitude_fim")
    private Double longitudeFim;
    
    @Schema(description = "Comprimento máximo da vaga em metros", example = "5", minimum = "5")
    @Column(nullable=false, precision = 5, scale = 2)
    private Integer comprimento;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusVagaEnum status = StatusVagaEnum.INDISPONIVEL;

    @OneToMany(mappedBy = "vaga", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<OperacaoVaga> operacoesVaga;

    @OneToMany(mappedBy = "vaga", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<DisponibilidadeVaga> disponibilidades;

    @Column(nullable = true)
    private Integer quantidade;

    public Vaga(
        EnderecoVaga endereco,
        AreaVagaEnum area,
        String numeroEndereco,
        String referenciaEndereco,
        TipoVagaEnum tipoVaga,
        Double latitudeInicio,
        Double longitudeInicio,
        Double latitudeFim,
        Double longitudeFim,
        Integer comprimento,
        Integer quantidade,
        Set<OperacaoVaga> operacoesVaga
    ){
        this.endereco = endereco;
        this.area = area;
        this.numeroEndereco = numeroEndereco;
        this.referenciaEndereco = referenciaEndereco;
        this.tipoVaga = tipoVaga;
        this.latitudeInicio = latitudeInicio;
        this.longitudeInicio = longitudeInicio;
        this.latitudeFim = latitudeFim;
        this.longitudeFim = longitudeFim;
        this.comprimento = comprimento;
        this.quantidade = quantidade;
        this.operacoesVaga = operacoesVaga;
    }

    public void setEndereco(EnderecoVaga endereco) {
        this.endereco = endereco;
    }

    public void setArea(AreaVagaEnum area) {
        this.area = area;
    }

    public void setNumeroEndereco(String numeroEndereco) {
        this.numeroEndereco = numeroEndereco;
    }

    public void setReferenciaEndereco(String referenciaEndereco) {
        this.referenciaEndereco = referenciaEndereco;
    }

    public void setTipoVaga(TipoVagaEnum tipoVaga) {
        this.tipoVaga = tipoVaga;
    }

    public void setLatitudeInicio(Double latitudeInicio) {
        this.latitudeInicio = latitudeInicio;
    }

    public void setLongitudeInicio(Double longitudeInicio) {
        this.longitudeInicio = longitudeInicio;
    }

    public void setLatitudeFim(Double latitudeFim) {
        this.latitudeFim = latitudeFim;
    }

    public void setLongitudeFim(Double longitudeFim) {
        this.longitudeFim = longitudeFim;
    }

    public void setComprimento(Integer comprimento) {
        this.comprimento = comprimento;
    }

    public void setStatus(StatusVagaEnum status) {
        this.status = status;
    }

    public void setOperacoesVaga(Set<OperacaoVaga> operacoesVaga) {
        if(operacoesVaga != null){
            if(this.operacoesVaga == null) this.operacoesVaga = new HashSet<>();
            else this.operacoesVaga.clear();
            this.operacoesVaga.addAll(operacoesVaga);
        }
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

}