package com.cptrans.petrocarga.modules.vaga.service;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.enums.DiaSemanaEnum;
import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.StatusVagaEnum;
import com.cptrans.petrocarga.enums.TipoVagaEnum;
import com.cptrans.petrocarga.modules.enderecoVaga.entity.EnderecoVaga;
import com.cptrans.petrocarga.modules.enderecoVaga.service.EnderecoVagaService;
import com.cptrans.petrocarga.modules.operacaoVaga.service.OperacaoVagaService;
import com.cptrans.petrocarga.modules.vaga.dto.mapper.VagaMapper;
import com.cptrans.petrocarga.modules.vaga.dto.projection.ClusterMapaProjection;
import com.cptrans.petrocarga.modules.vaga.dto.request.VagaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.vaga.dto.request.VagaPatchDTO;
import com.cptrans.petrocarga.modules.vaga.dto.request.VagaRequestDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagaCoordenadaResponseDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagaResponseDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagasClusterResponseDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagasMapaResponseDTO;
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

    private static final int ZOOM_MINIMO_VAGAS_INDIVIDUAIS = 13;
    public static final int LIMITE_VAGAS_MAPA = 500;

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

    public VagasMapaResponseDTO buscarPorMapa(
        Double north,
        Double south,
        Double east,
        Double west,
        Double zoom,
        StatusVagaEnum status
    ) {
        if (zoom < ZOOM_MINIMO_VAGAS_INDIVIDUAIS) {
            return buscarClustersVagas(
                north,
                south,
                east,
                west,
                zoom,
                status
            );
        }

        return buscarVagasIndividuais(
            north,
            south,
            east,
            west,
            status
        );
    }

    private VagasMapaResponseDTO buscarVagasIndividuais(
        Double north,
        Double south,
        Double east,
        Double west,
        StatusVagaEnum status
    ) {

        Pageable limite = PageRequest.of(0, LIMITE_VAGAS_MAPA);

        List<Vaga> vagas;
        Set<UUID> idsDisponiveisHoje = Set.of();

        if (status == StatusVagaEnum.DISPONIVEL) {

            OffsetDateTime agora = DateUtils.agora();

            LocalDate dataHoje = agora.toLocalDate();
            LocalDate dataDepoisAmanha = dataHoje.plusDays(2);

            int offsetSegundos = agora.getOffset().getTotalSeconds();

            vagas = vagaRepository.buscarDisponiveisPorArea(
                north,
                south,
                east,
                west,
                agora,
                dataHoje,
                dataDepoisAmanha,
                offsetSegundos,
                limite
            );

            if (!vagas.isEmpty()) {

                String diaSemana = DiaSemanaEnum.fromDayOfWeek(dataHoje.getDayOfWeek()).name();

                Set<UUID> ids = vagas.stream().map(Vaga::getId).collect(Collectors.toSet());

                idsDisponiveisHoje = vagaRepository.buscarIdsDisponiveisHoje(
                        ids,
                        diaSemana,
                        agora,
                        offsetSegundos
                    );
            }

        } else {

            vagas = vagaRepository.buscarPorArea(
                north,
                south,
                east,
                west,
                status,
                limite
            );
        }
        
        List<VagaCoordenadaResponseDTO> response = vagaMapper.toCoordenadaResponseList(vagas, status, idsDisponiveisHoje);
        
        return vagaMapper.toVagasMapaResponse(response, null);
    }

    private VagasMapaResponseDTO buscarClustersVagas(
        Double north,
        Double south,
        Double east,
        Double west,
        Double zoom,
        StatusVagaEnum status
    ) {

        double tamanhoCelula = calcularTamanhoCelula(zoom);

        List<ClusterMapaProjection> clusters;

        if (status == StatusVagaEnum.DISPONIVEL) {
            OffsetDateTime agora = DateUtils.agora();

            LocalDate dataHoje = agora.toLocalDate();
            LocalDate dataDepoisAmanha = dataHoje.plusDays(2);

            int offsetSegundos = agora.getOffset().getTotalSeconds();

            clusters = vagaRepository.buscarClustersDisponiveisPorArea(
                north,
                south,
                east,
                west,
                agora,
                dataHoje,
                dataDepoisAmanha,
                offsetSegundos,
                tamanhoCelula
            );

        } else {
            clusters = vagaRepository.buscarClustersPorArea(
                north,
                south,
                east,
                west,
                status,
                tamanhoCelula
            );
        }

        List<VagasClusterResponseDTO> response = vagaMapper.toClusterResponseList(clusters);

        return vagaMapper.toVagasMapaResponse(null, response);
    }
    
    private double calcularTamanhoCelula(Double zoom) {
        return ((360.0 / 256.0) * 64.0) / Math.pow(2, zoom);
    }

}