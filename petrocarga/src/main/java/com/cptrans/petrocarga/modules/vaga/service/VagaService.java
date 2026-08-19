package com.cptrans.petrocarga.modules.vaga.service;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.StatusVagaEnum;
import com.cptrans.petrocarga.enums.TipoVagaEnum;
import com.cptrans.petrocarga.modules.enderecoVaga.entity.EnderecoVaga;
import com.cptrans.petrocarga.modules.enderecoVaga.service.EnderecoVagaService;
import com.cptrans.petrocarga.modules.operacaoVaga.service.OperacaoVagaService;
import com.cptrans.petrocarga.modules.vaga.dto.mapper.VagaMapper;
import com.cptrans.petrocarga.modules.vaga.dto.request.VagaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.vaga.dto.request.VagaPatchDTO;
import com.cptrans.petrocarga.modules.vaga.dto.request.VagaRequestDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagaCoordenadaResponseDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagaResponseDTO;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
import com.cptrans.petrocarga.modules.vaga.exceptions.VagaExceptions;
import com.cptrans.petrocarga.modules.vaga.repository.VagaRepository;
import com.cptrans.petrocarga.modules.vaga.specification.VagaSpecification;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor; 

@Service
@RequiredArgsConstructor
public class VagaService {
    private final VagaRepository vagaRepository;
    private final EnderecoVagaService enderecoVagaService;
    private final OperacaoVagaService operacaoVagaService;
    private final VagaMapper vagaMapper;

    private final Sort SORT_ASC = Sort.by("endereco.logradouro").ascending();
    private final Sort SORT_DESC = Sort.by("endereco.logradouro").descending();

    public List<Vaga> findAll() {
        return vagaRepository.findAll();
    }

    public List<Vaga> findAllByStatus(StatusVagaEnum status) {
        if (status != null && status.equals(StatusVagaEnum.DISPONIVEL)){
            return vagaRepository.buscarDisponiveis();
        }
        return vagaRepository.findByStatus(status);
    }
    
    public PageResponseDTO findAllPaginadas(VagaFiltrosRequestDTO filtros, Integer numeroPagina, Integer tamanhoPagina, OrdemEnum ordem) {
        Pageable pageable = PageRequest.of(numeroPagina, tamanhoPagina, !ordem.equals(OrdemEnum.ASC) ? SORT_DESC : SORT_ASC);
        Page<VagaResponseDTO> vagasPage = vagaRepository.findAll(VagaSpecification.filtrar(filtros),pageable).map(vagaMapper::toResponse);
        return new PageResponseDTO(vagasPage);
    }

    public Vaga findById(UUID id) {
        return vagaRepository.findById(id).orElseThrow(() -> new VagaExceptions.VagaNotFoundException());
    }

    public List<Vaga> findByIdIn(List<UUID> listaIds) {
        return vagaRepository.findByIdIn(listaIds);
    }
    
    public void deleteById(UUID id) {
        Vaga vaga = findById(id);
        vagaRepository.deleteById(vaga.getId());
    }

    @Transactional
    public Vaga updateById(UUID id, VagaPatchDTO request) {
        Vaga vagaExistente = findById(id);

        if (request.getEndereco() != null){
            EnderecoVaga novoEndereco = enderecoVagaService.cadastrarEnderecoVaga(request.getEndereco());
            vagaExistente.setEndereco(novoEndereco);
        }

        if (request.getTipoVaga() != null) {
            if (request.getTipoVaga().equals(TipoVagaEnum.PERPENDICULAR)) {
                if ((request.getQuantidade() == null || request.getQuantidade() <= 0)) {
                    throw new VagaExceptions.QuantidadePosicoesInvalidaException();
                }
            } else {
                request.setQuantidade(null);
                vagaExistente.setQuantidade(null);
            }
            vagaExistente.setTipoVaga(request.getTipoVaga());
        }
        
        if (request.getQuantidade() != null && request.getQuantidade() > 0 && vagaExistente.getTipoVaga().equals(TipoVagaEnum.PERPENDICULAR)) {
            vagaExistente.setQuantidade(request.getQuantidade());
        }

        if (request.getArea() != null) vagaExistente.setArea(request.getArea());
        if (request.getNumeroEndereco() != null) vagaExistente.setNumeroEndereco(request.getNumeroEndereco());
        if (request.getReferenciaEndereco() != null) vagaExistente.setReferenciaEndereco(request.getReferenciaEndereco());
        if (request.getLatitudeInicio() != null) vagaExistente.setLatitudeInicio(request.getLatitudeInicio());
        if (request.getLongitudeInicio() != null) vagaExistente.setLongitudeInicio(request.getLongitudeInicio());
        if (request.getLatitudeFim() != null) vagaExistente.setLatitudeFim(request.getLatitudeFim());
        if (request.getLongitudeFim() != null) vagaExistente.setLongitudeFim(request.getLongitudeFim());
        if (request.getComprimento() != null) vagaExistente.setComprimento(request.getComprimento());
        if (request.getStatus() != null) vagaExistente.setStatus(request.getStatus());

        if (request.getOperacoesVaga() != null) {
            operacaoVagaService.atualizarOperacoesVaga(request.getOperacoesVaga(), vagaExistente);
        }

        return vagaRepository.save(vagaExistente);
    }

    @Transactional()
    public Vaga createVaga(VagaRequestDTO request){

        if (!request.getTipoVaga().equals(TipoVagaEnum.PERPENDICULAR)) request.setQuantidade(null);

        if (
            (request.getTipoVaga().equals(TipoVagaEnum.PERPENDICULAR)) &&
            (request.getQuantidade() == null || request.getQuantidade() <= 0)
        ) throw new VagaExceptions.QuantidadePosicoesInvalidaException();

        EnderecoVaga enderecoCadastrado = enderecoVagaService.cadastrarEnderecoVaga(request.getEndereco());

        Vaga vagaCadastrada = vagaRepository.save(vagaMapper.toEntity(request, enderecoCadastrado));

        return vagaCadastrada;
    }

    public List<VagaCoordenadaResponseDTO> buscarPorMapa(
        Double north,
        Double south,
        Double east,
        Double west,
        StatusVagaEnum status
    ) {
        if (status != null && status.equals(StatusVagaEnum.DISPONIVEL)){
            OffsetDateTime agora = DateUtils.agora();
            return vagaRepository.buscarDisponiveisPorArea(
                south, north, west, east, agora, agora.plusDays(2)
            )
            .stream()
            .map(v -> vagaMapper.toCoordenadaResponse(v, status)).filter(v -> v != null).toList();
        }
        return vagaRepository.buscarPorArea(
            south, north, west, east, status
        ).stream().map(v -> vagaMapper.toCoordenadaResponse(v, status)).toList();
    }

    

}