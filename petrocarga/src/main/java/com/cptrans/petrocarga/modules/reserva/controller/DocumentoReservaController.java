package com.cptrans.petrocarga.modules.reserva.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.config.swagger.response.DefaultResponses;
import com.cptrans.petrocarga.config.swagger.response.GetResponses;
import com.cptrans.petrocarga.modules.reserva.entity.Reserva;
import com.cptrans.petrocarga.modules.reserva.service.DocumentoReservaService;
import com.cptrans.petrocarga.modules.reserva.service.ReservaService;
import com.cptrans.petrocarga.security.UserAuthenticated;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Reservas - Documentos" , description = "Endpoints para gerenciamento de documentos de reservas")
@RequestMapping("/documentos/reservas")
@RequiredArgsConstructor
public class DocumentoReservaController {
    private final DocumentoReservaService documentoReservaService;
    private final ReservaService reservaService;

    @Operation(
        summary = "Gerar comprovante de reserva",
        description = "Retorna o comprovante de reserva em formato PDF"
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'MOTORISTA', 'EMPRESA')")
    @GetMapping(
        value = "/{id}/comprovante",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> gerarComprovante(
        @Parameter(description = "Usuário autenticado")
        @AuthenticationPrincipal UserAuthenticated user, 
        
        @Parameter(description = "ID da reserva")
        @PathVariable UUID id
    ) throws IOException {
        Reserva reserva = reservaService.findById(id);
        
        String html = documentoReservaService.gerarHtmlReserva(reserva);
        byte[] pdf = documentoReservaService.gerarPdf(html);

        return ResponseEntity.ok().header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=comprovante-reserva.pdf")
                .body(pdf);
    }
}