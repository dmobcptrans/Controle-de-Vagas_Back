package com.cptrans.petrocarga.application.usecase;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.domain.entities.EnderecoVaga;
import com.cptrans.petrocarga.domain.repositories.EnderecoVagaRepository;

@Service
public class EnderecoVagaService {
    
    @Autowired
    private EnderecoVagaRepository enderecoVagaRepository;

    public EnderecoVaga cadastrarEnderecoVaga(EnderecoVaga novoEndereco){
        Optional<EnderecoVaga> enderecoCadrastado = enderecoVagaRepository.findByCodigoPmp(novoEndereco.getCodigoPmp());
        if(enderecoCadrastado.isPresent()){
           return enderecoCadrastado.get();
        }
        if(novoEndereco.getCodigoPmp() == null || novoEndereco.getCodigoPmp().isEmpty()) {
            throw new IllegalArgumentException("O campo 'codigoPMP' é obrigatório e não pode ser nulo ou vazio.");
        }
        if(novoEndereco.getLogradouro() == null || novoEndereco.getLogradouro().isEmpty()) {
            throw new IllegalArgumentException("O campo 'logradouro' é obrigatório e não pode ser nulo ou vazio.");
        }
        if(novoEndereco.getBairro() == null || novoEndereco.getBairro().isEmpty()) {
            throw new IllegalArgumentException("O campo 'bairro' é obrigatório e não pode ser nulo ou vazio.");
        }
    
        return enderecoVagaRepository.save(novoEndereco);
    }

}
