package com.cptrans.petrocarga.application.usecase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.cptrans.petrocarga.application.dto.ReservaDetailedResponseDTO;
import com.cptrans.petrocarga.domain.entities.Reserva;
import com.cptrans.petrocarga.domain.repositories.ReservaRepository;
import com.cptrans.petrocarga.shared.utils.DateUtils;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Service
public class DocumentoReservaService {

    private final TemplateEngine templateEngine;
    
    @Autowired
    private ReservaRepository reservaRepository;

    public Reserva findReservaWithDetails(UUID id) {
        return reservaRepository.findByIdWithJoins(id);
    }

    public DocumentoReservaService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String gerarHtmlReserva(Reserva reserva) {
        Context context = new Context();
        context.setVariable("reserva", new ReservaDetailedResponseDTO(reserva));
        context.setVariable("agora", OffsetDateTime.now(DateUtils.FUSO_BRASIL));

        return templateEngine.process(
                "pdf/comprovante-reserva",
                context
        );
    }

    public byte[] gerarPdf(String html) throws IOException {
        String baseUri = Paths.get("src/main/resources/static").toUri().toString();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(html, baseUri);
        builder.toStream(out);
        builder.run();


        return out.toByteArray();
    }

}