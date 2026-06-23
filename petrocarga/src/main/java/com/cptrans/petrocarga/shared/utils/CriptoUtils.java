package com.cptrans.petrocarga.shared.utils;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.cripto.CriptoService;
import com.cptrans.petrocarga.modules.denuncia.dto.response.DenunciaResponseDTO;
import com.cptrans.petrocarga.modules.motorista.dto.response.MotoristaResponseDTO;
import com.cptrans.petrocarga.modules.usuario.dto.response.UsuarioResponseDTO;
import com.cptrans.petrocarga.modules.veiculo.dto.response.VeiculoResponseDTO;

@Component
public class CriptoUtils {
    
    private static CriptoService criptoService;

    public CriptoUtils(CriptoService criptoService) {
        CriptoUtils.criptoService = criptoService;
     }

    public static UsuarioResponseDTO decrypt(UsuarioResponseDTO userResponse, Integer keyVersion){
        if (userResponse != null){
            if (userResponse.getCpf() != null && userResponse.getCpf().length() > 11) userResponse.setCpf(criptoService.decrypt(userResponse.getCpf(), keyVersion));
            if (userResponse.getTelefone() != null && userResponse.getTelefone().length() > 11) userResponse.setTelefone(criptoService.decrypt(userResponse.getTelefone(), keyVersion));
            if (userResponse.getEmail() != null && !userResponse.getEmail().contains("@")) userResponse.setEmail(criptoService.decrypt(userResponse.getEmail(), keyVersion));
        }
        return userResponse;   
    }

    public static  MotoristaResponseDTO decrypt(MotoristaResponseDTO motoristaResponse, Integer keyVersion){
        if (motoristaResponse != null){
            if (motoristaResponse.getUsuario() != null){
                UsuarioResponseDTO usuarioResponse = decrypt(motoristaResponse.getUsuario(), keyVersion);
                motoristaResponse.setUsuario(usuarioResponse);
            }
            if (motoristaResponse.getNumeroCnh() != null) motoristaResponse.setNumeroCnh(criptoService.decrypt(motoristaResponse.getNumeroCnh(), keyVersion));
        }  
        return motoristaResponse;
    }

    public static  VeiculoResponseDTO decrypt(VeiculoResponseDTO veiculoResponse, Integer keyVersion){
        if (veiculoResponse != null){
            if (veiculoResponse.getCpfProprietario() != null) veiculoResponse.setCpfProprietario(criptoService.decrypt(veiculoResponse.getCpfProprietario(), keyVersion));
        }
        return veiculoResponse;
    }
    
    public static  DenunciaResponseDTO decrypt (DenunciaResponseDTO denunciaResponse, Integer keyVersion) {
        if (denunciaResponse != null) {
            if (denunciaResponse.getTelefoneMotorista() != null) {
                denunciaResponse.setTelefoneMotorista(criptoService.decrypt(denunciaResponse.getTelefoneMotorista(), keyVersion));
            }
        }
        return denunciaResponse;
    }
}