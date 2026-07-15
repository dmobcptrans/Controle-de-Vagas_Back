package com.cptrans.petrocarga.shared.utils;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.cripto.CriptoService;
import com.cptrans.petrocarga.modules.denuncia.dto.response.DenunciaResponseDTO;
import com.cptrans.petrocarga.modules.motorista.dto.response.MotoristaResponseDTO;
import com.cptrans.petrocarga.modules.usuario.dto.response.UsuarioResponseDTO;
import com.cptrans.petrocarga.modules.veiculo.dto.response.VeiculoResponseDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CriptoUtils {
    
    private final CriptoService criptoService;

    public UsuarioResponseDTO decrypt(UsuarioResponseDTO userResponse, Integer keyVersion){
        if (userResponse != null){
            if (userResponse.getTelefone() != null && userResponse.getTelefone().length() > 11) userResponse.setTelefone(criptoService.decrypt(userResponse.getTelefone(), keyVersion));
            if (userResponse.getEmail() != null && !userResponse.getEmail().contains("@")) userResponse.setEmail(criptoService.decrypt(userResponse.getEmail(), keyVersion));
        }
        return userResponse;   
    }

    public  MotoristaResponseDTO decrypt(MotoristaResponseDTO motoristaResponse, Integer keyVersion){
        if (motoristaResponse != null){
            if (motoristaResponse.getUsuario() != null){
                UsuarioResponseDTO usuarioResponse = decrypt(motoristaResponse.getUsuario(), keyVersion);
                motoristaResponse.setUsuario(usuarioResponse);
            }
            if (motoristaResponse.getNumeroCnh() != null) motoristaResponse.setNumeroCnh(criptoService.decrypt(motoristaResponse.getNumeroCnh(), keyVersion));
        }  
        return motoristaResponse;
    }

    public  VeiculoResponseDTO decrypt(VeiculoResponseDTO veiculoResponse, Integer keyVersion){
        if (veiculoResponse != null){
            if (veiculoResponse.getCpfProprietario() != null) veiculoResponse.setCpfProprietario(criptoService.decrypt(veiculoResponse.getCpfProprietario(), keyVersion));
        }
        return veiculoResponse;
    }
    
    public  DenunciaResponseDTO decrypt (DenunciaResponseDTO denunciaResponse, Integer keyVersion) {
        if (denunciaResponse != null) {
            if (denunciaResponse.getTelefoneMotorista() != null) {
                denunciaResponse.setTelefoneMotorista(criptoService.decrypt(denunciaResponse.getTelefoneMotorista(), keyVersion));
            }
        }
        return denunciaResponse;
    }

    public String decrypt(String value, Integer keyVersion) {
        if (value != null && !value.isEmpty()) {
            return criptoService.decrypt(value, keyVersion);
        }
        return value;
    }
}