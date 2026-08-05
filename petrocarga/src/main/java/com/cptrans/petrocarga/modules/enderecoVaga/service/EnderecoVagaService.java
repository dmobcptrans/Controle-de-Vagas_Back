package com.cptrans.petrocarga.modules.enderecoVaga.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.modules.enderecoVaga.dto.mapper.EnderecoVagaMapper;
import com.cptrans.petrocarga.modules.enderecoVaga.dto.request.EnderecoVagaRequestDTO;
import com.cptrans.petrocarga.modules.enderecoVaga.dto.response.EnderecoVagaResponseDTO;
import com.cptrans.petrocarga.modules.enderecoVaga.entity.EnderecoVaga;
import com.cptrans.petrocarga.modules.enderecoVaga.exceptions.EnderecoVagaExceptions;
import com.cptrans.petrocarga.modules.enderecoVaga.repository.EnderecoVagaRepository;
import com.cptrans.petrocarga.shared.utils.StringUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnderecoVagaService {
    
    private final EnderecoVagaRepository enderecoVagaRepository;
    private final EnderecoVagaMapper enderecoVagaMapper;

    public Set<String> getCodigosPmp() {
        return enderecoVagaRepository.findAllCodigosPmp();
    }

    public List<EnderecoVagaResponseDTO> getEnderecosVaga() {
        return enderecoVagaMapper.toResponseList(enderecoVagaRepository.findAll());
    }

    public EnderecoVaga findByCodigoPmp(String codigoPmp) {
        return enderecoVagaRepository.findByCodigoPmpIgnoreCase(codigoPmp.trim()).orElseThrow(() -> new EnderecoVagaExceptions.EnderecoVagaNotFoundException());
    }

    public EnderecoVagaResponseDTO getEnderecoVagaByCodigoPmp(String codigoPmp) {
        return enderecoVagaMapper.toResponse(findByCodigoPmp(codigoPmp.trim()));
    }

    public EnderecoVaga cadastrarEnderecoVaga(@Valid EnderecoVagaRequestDTO request){
        Optional<EnderecoVaga> enderecoCadrastado = enderecoVagaRepository.findByCodigoPmpIgnoreCase(request.getCodigoPmp().trim());
        if (enderecoCadrastado.isPresent()) return enderecoCadrastado.get();
        
        EnderecoVaga novoEndereco = new EnderecoVaga(
            StringUtils.formatarNome(request.getLogradouro().trim()), 
            StringUtils.formatarNome(request.getBairro().trim()), 
            request.getCodigoPmp().trim().toUpperCase()
        );
    
        return enderecoVagaRepository.save(novoEndereco);
    }
}