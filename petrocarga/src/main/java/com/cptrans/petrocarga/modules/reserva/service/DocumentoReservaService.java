package com.cptrans.petrocarga.modules.reserva.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.cptrans.petrocarga.modules.reserva.dto.mapper.ReservaMapper;
import com.cptrans.petrocarga.modules.reserva.entity.Reserva;
import com.cptrans.petrocarga.shared.utils.DateUtils;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentoReservaService {
    private final TemplateEngine templateEngine;
    private final ReservaMapper reservaMapper;

    public String gerarHtmlReserva(Reserva reserva) {
        Context context = new Context();
        context.setVariable("reserva", reservaMapper.toDetailedResponse(reserva));
        context.setVariable("agora", DateUtils.agora());

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