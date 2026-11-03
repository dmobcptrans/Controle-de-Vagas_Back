package com.cptrans.petrocarga.modules.enderecoVaga.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.modules.enderecoVaga.dto.mapper.EnderecoVagaMapper;
import com.cptrans.petrocarga.modules.enderecoVaga.dto.request.EnderecoVagaRequestDTO;
import com.cptrans.petrocarga.modules.enderecoVaga.dto.response.EnderecoVagaResponseDTO;
import com.cptrans.petrocarga.modules.enderecoVaga.entity.EnderecoVaga;
import com.cptrans.petrocarga.modules.enderecoVaga.exceptions.EnderecoVagaExceptions.EnderecoVagaAlreadyExistsException;
import com.cptrans.petrocarga.modules.enderecoVaga.repository.EnderecoVagaRepository;
import com.cptrans.petrocarga.shared.utils.StringUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnderecoVagaService {
    
    private final EnderecoVagaRepository enderecoVagaRepository;
    private final EnderecoVagaMapper enderecoVagaMapper;

    public List<EnderecoVagaResponseDTO> getEnderecosVaga() {
        return enderecoVagaMapper.toResponseList(enderecoVagaRepository.findAll());
    }

    public EnderecoVaga cadastrarEnderecoVaga(@Valid EnderecoVagaRequestDTO request){
        String logradouro = StringUtils.formatarNome(request.getLogradouro().trim());
        String bairro = StringUtils.formatarNome(request.getBairro().trim());
        
        Optional<EnderecoVaga> enderecoCadrastado = enderecoVagaRepository.findByLogradouroIgnoreCase(logradouro);
        
        if (enderecoCadrastado.isPresent()){
            if (enderecoCadrastado.get().getBairro().equalsIgnoreCase(bairro)){
                return enderecoCadrastado.get();
            } else {
                throw new EnderecoVagaAlreadyExistsException();
            }
        }
        
        EnderecoVaga novoEndereco = new EnderecoVaga(
            logradouro,
            bairro
        );
    
        return enderecoVagaRepository.save(novoEndereco);
    }
}