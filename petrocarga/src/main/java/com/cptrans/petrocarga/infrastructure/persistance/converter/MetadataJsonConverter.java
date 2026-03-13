package com.cptrans.petrocarga.infrastructure.persistance.converter;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class MetadataJsonConverter implements AttributeConverter<Map<String, Object>, String> {
    private static final ObjectMapper mapper = JsonMapper.builder()
    .addModule(new JavaTimeModule())
    .build();


/**
 * Converte um mapa de metadata para uma string no formato JSON.
 * 
 * @param attribute o mapa de metadata a ser convertido
 * @return a string no formato JSON representando o mapa de metadata
 * @throws IllegalArgumentException se ocorrer um erro durante a conversão
 */
    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {

        try {
            return attribute == null ? null : mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Erro ao converter metadata para JSON");
        }
    }

    /**
     * Converte uma string no formato JSON para um mapa de metadata.
     * 
     * @param dbData a string no formato JSON representando o mapa de metadata
     * @return o mapa de metadata convertido
     * @throws IllegalArgumentException se ocorrer um erro durante a convers o
     */
    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : mapper.readValue(dbData, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Erro ao converter JSON para metadata");
        }
    }
}
