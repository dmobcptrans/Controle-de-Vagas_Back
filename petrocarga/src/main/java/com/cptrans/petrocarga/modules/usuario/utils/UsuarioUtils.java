package com.cptrans.petrocarga.modules.usuario.utils;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.modules.agente.entity.Agente;
import com.cptrans.petrocarga.modules.agente.repository.AgenteRepository;
import com.cptrans.petrocarga.modules.empresa.entity.Empresa;
import com.cptrans.petrocarga.modules.empresa.repository.EmpresaRepository;
import com.cptrans.petrocarga.modules.gestor.entity.Gestor;
import com.cptrans.petrocarga.modules.gestor.repository.GestorRepository;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.motorista.repository.MotoristaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UsuarioUtils {
    private final EmpresaRepository empresaRepository;
    private final MotoristaRepository motoristaRepository;
    private final AgenteRepository agenteRepository;
    private final GestorRepository gestorRepository;

    public final static String VERSAO_ATUAL_TERMOS = "1.0.0";
    public static String gerarLastN(String string, int n) {
        return string.substring(string.length() - n);
    }

    public static String getCpf(Motorista motorista) {
        if (motorista != null && motorista.getCpfCripto() != null) return motorista.getCpfCripto();
        return null;
    }

    public static String getCpf(Empresa empresa) {
        if (empresa != null && empresa.getCnpj() != null) return empresa.getCnpj();
        return null;
    }

    public static String getCpf(Gestor gestor) {
        if (gestor != null && gestor.getCpfCripto() != null) return gestor.getCpfCripto();
        return null;
    }

    public static String getCpf(Agente agente){
        if (agente != null && agente.getCpfCripto() != null) return agente.getCpfCripto();
        return null;
    }

    public String getCpfOrCnpjByPermissao(PermissaoEnum permissao, UUID usuarioId) {
        switch (permissao) {
            case ADMIN:
                return null;
            case GESTOR:
                return gestorRepository.findCpfCriptoById(usuarioId).orElse(null);
            case MOTORISTA:
                return motoristaRepository.findCpfCriptoById(usuarioId).orElse(null);
            case EMPRESA:
                return empresaRepository.findCnpjById(usuarioId).orElse(null);
            case AGENTE:
                return agenteRepository.findCpfCriptoById(usuarioId).orElse(null);
            default:
                return null;
        }
    }
}