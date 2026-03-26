package com.cptrans.petrocarga.shared.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.application.dto.DenunciaResponseDTO;
import com.cptrans.petrocarga.application.dto.MotoristaResponseDTO;
import com.cptrans.petrocarga.application.dto.UsuarioResponseDTO;
import com.cptrans.petrocarga.application.dto.VeiculoResponseDTO;
import com.cptrans.petrocarga.infrastructure.security.CriptoService;

@Component
public class CriptoUtils {
    @Autowired
    private CriptoService criptoService;

    public UsuarioResponseDTO decrypt(UsuarioResponseDTO userResponse){
        if(userResponse != null){
            if(userResponse.getCpf() != null) userResponse.setCpf(criptoService.decrypt(userResponse.getCpf(), criptoService.getActiveKeyVersion()));
            if(userResponse.getTelefone() != null) userResponse.setTelefone(criptoService.decrypt(userResponse.getTelefone(), criptoService.getActiveKeyVersion()));
            if(userResponse.getEmail() != null) userResponse.setEmail(criptoService.decrypt(userResponse.getEmail(), criptoService.getActiveKeyVersion()));
        }
        return userResponse;   
    }

    public MotoristaResponseDTO decrypt(MotoristaResponseDTO motoristaResponse){
        if(motoristaResponse != null){
            if(motoristaResponse.getUsuario() != null){
                UsuarioResponseDTO usuarioResponse = motoristaResponse.getUsuario();
                if (usuarioResponse.getCpf() != null) usuarioResponse.setCpf(criptoService.decrypt(usuarioResponse.getCpf(), criptoService.getActiveKeyVersion()));
                if (usuarioResponse.getTelefone() != null ) usuarioResponse.setTelefone(criptoService.decrypt(usuarioResponse.getTelefone(), criptoService.getActiveKeyVersion()));
                if(usuarioResponse.getEmail() != null) usuarioResponse.setEmail(criptoService.decrypt(usuarioResponse.getEmail(), criptoService.getActiveKeyVersion()));
            }
            if(motoristaResponse.getNumeroCnh() != null) motoristaResponse.setNumeroCnh(criptoService.decrypt(motoristaResponse.getNumeroCnh(), criptoService.getActiveKeyVersion()));
        }
        return motoristaResponse;
    }

    public VeiculoResponseDTO decrypt(VeiculoResponseDTO veiculoResponse){
        if(veiculoResponse != null){
            if(veiculoResponse.getCpfProprietario() != null) veiculoResponse.setCpfProprietario(criptoService.decrypt(veiculoResponse.getCpfProprietario(), criptoService.getActiveKeyVersion()));
        }
        return veiculoResponse;
    }
    
    public DenunciaResponseDTO decrypt (DenunciaResponseDTO denunciaResponse) {
        if(denunciaResponse != null) {
            if(denunciaResponse.getTelefoneMotorista() != null) {
                denunciaResponse.setTelefoneMotorista(criptoService.decrypt(denunciaResponse.getTelefoneMotorista(), criptoService.getActiveKeyVersion()));
            }
        }
        return denunciaResponse;
    }
}
