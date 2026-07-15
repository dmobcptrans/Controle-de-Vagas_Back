package com.cptrans.petrocarga.shared.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;

public class PageResponseDTO {
    @Schema(description = "Conteúdo da página", example = "[{}]")
    List<?> content;
    @Schema(description = "Total de elementos", example = "0")
    Long totalElementos;
    @Schema(description = "Total de páginas", example = "1")
    int totalPaginas;
    @Schema(description = "Tamanho da página", example = "10")
    int tamanhoPagina;
    @Schema(description = "Número da página", example = "0")
    int pagina;

    public PageResponseDTO(List<?> content, Long totalElements, int totalPages, int size, int number) {
        this.content = content;
        this.totalElementos = totalElements;
        this.totalPaginas = totalPages;
        this.tamanhoPagina = size;
        this.pagina = number;
    }

    public PageResponseDTO(Page<?> page){
        if (page == null) return;
        this.content = page.getContent();
        this.totalElementos = page.getTotalElements();
        this.totalPaginas = page.getTotalPages();
        this.tamanhoPagina = page.getSize();
        this.pagina = page.getNumber();
    }

    public List<?> getContent() {
        return content;
    }

    public void setContent(List<?> content) {
        this.content = content;
    }

    public Long getTotalElementos() {
        return totalElementos;
    }

    public void setTotalElementos(Long totalElementos) {
        this.totalElementos = totalElementos;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public void setTotalPaginas(int totalPaginas) {
        this.totalPaginas = totalPaginas;
    }

    public int getTamanhoPagina() {
        return tamanhoPagina;
    }

    public void setTamanhoPagina(int tamanhoPagina) {
        this.tamanhoPagina = tamanhoPagina;
    }

    public int getPagina() {
        return pagina;
    }

    public void setPagina(int pagina) {
        this.pagina = pagina;
    }
}