package com.cptrans.petrocarga.interfaces.controllers;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.application.dto.ReservaDetailedResponseDTO;
import com.cptrans.petrocarga.application.usecase.DocumentoReservaService;
import com.cptrans.petrocarga.application.usecase.ReservaService;
import com.cptrans.petrocarga.domain.entities.Reserva;
import com.cptrans.petrocarga.domain.enums.PermissaoEnum;
import com.cptrans.petrocarga.infrastructure.security.UserAuthenticated;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/documentos/reservas")
public class DocumentoReservaController {

    @Autowired
    private DocumentoReservaService documentoReservaService;

    @Autowired
    private ReservaService reservaService;

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'MOTORISTA', 'EMPRESA')")
    @GetMapping("/{id}")
    public ResponseEntity<ReservaDetailedResponseDTO> getDocumentoReservaById(@PathVariable UUID id) {
        Reserva reserva = documentoReservaService.findReservaWithDetails(id);
        ReservaDetailedResponseDTO dto = new ReservaDetailedResponseDTO(reserva);
        return ResponseEntity.ok(dto);
    }

/**
 * Gera um PDF contendo os detalhes da reserva.
 * @param id O ID da reserva.
 * @return O PDF gerado como um anexo.
 * @throws EntityNotFoundException se a reserva não for encontrada.
 * @throws IOException se ocorrer um erro ao gerar o PDF.
 * @param user O usuário autenticado que requisitou o PDF.
 */
    @GetMapping(
    value = "/{id}/comprovante",
    produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> gerarComprovante(@AuthenticationPrincipal UserAuthenticated user, @PathVariable UUID id) throws IOException {
        List<String> authorities = user.userDetails().getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        Reserva reserva = reservaService.findById(id);
        
        if (reserva != null) {
            if(authorities.contains(PermissaoEnum.MOTORISTA.getRole())){
                if(!reserva.getMotorista().getUsuario().getId().equals(user.id()) && !reserva.getCriadoPor().getId().equals(user.id())) throw new EntityNotFoundException("Reserva não encontrada.");
            } 
        }

        String html = documentoReservaService.gerarHtmlReserva(reserva);
        byte[] pdf = documentoReservaService.gerarPdf(html);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=comprovante-reserva.pdf")
                .body(pdf);
    }

}