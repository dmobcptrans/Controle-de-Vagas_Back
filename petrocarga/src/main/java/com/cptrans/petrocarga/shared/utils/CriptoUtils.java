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

    public UsuarioResponseDTO decrypt(UsuarioResponseDTO userResponse, Integer keyVersion){
        if(userResponse != null){
            if(userResponse.getCpf() != null) userResponse.setCpf(criptoService.decrypt(userResponse.getCpf(), keyVersion));
            if(userResponse.getTelefone() != null) userResponse.setTelefone(criptoService.decrypt(userResponse.getTelefone(), keyVersion));
            if(userResponse.getEmail() != null) userResponse.setEmail(criptoService.decrypt(userResponse.getEmail(), keyVersion));
        }
        return userResponse;   
    }

    public MotoristaResponseDTO decrypt(MotoristaResponseDTO motoristaResponse, Integer keyVersion){
        if(motoristaResponse != null){
            if(motoristaResponse.getUsuario() != null){
                UsuarioResponseDTO usuarioResponse = motoristaResponse.getUsuario();
                if (usuarioResponse.getCpf() != null) usuarioResponse.setCpf(criptoService.decrypt(usuarioResponse.getCpf(), keyVersion));
                if (usuarioResponse.getTelefone() != null ) usuarioResponse.setTelefone(criptoService.decrypt(usuarioResponse.getTelefone(), keyVersion));
                if(usuarioResponse.getEmail() != null) usuarioResponse.setEmail(criptoService.decrypt(usuarioResponse.getEmail(), keyVersion));
            }
            if(motoristaResponse.getNumeroCnh() != null) motoristaResponse.setNumeroCnh(criptoService.decrypt(motoristaResponse.getNumeroCnh(), keyVersion));
        }
        return motoristaResponse;
    }

    public VeiculoResponseDTO decrypt(VeiculoResponseDTO veiculoResponse, Integer keyVersion){
        if(veiculoResponse != null){
            if(veiculoResponse.getCpfProprietario() != null) veiculoResponse.setCpfProprietario(criptoService.decrypt(veiculoResponse.getCpfProprietario(), keyVersion));
        }
        return veiculoResponse;
    }
    
    public DenunciaResponseDTO decrypt (DenunciaResponseDTO denunciaResponse, Integer keyVersion) {
        if(denunciaResponse != null) {
            if(denunciaResponse.getTelefoneMotorista() != null) {
                denunciaResponse.setTelefoneMotorista(criptoService.decrypt(denunciaResponse.getTelefoneMotorista(), keyVersion));
            }
        }
        return denunciaResponse;
    }
}
