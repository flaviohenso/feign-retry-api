package com.example.feignretryapi.infrastructure.mapper;

import com.example.feignretryapi.application.dto.ProductResponse;
import com.example.feignretryapi.domain.entity.Product;
import com.example.feignretryapi.infrastructure.client.feign.dto.ExternalProductDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Mapper para conversão entre DTOs externos, entidades de domínio e DTOs de resposta.
 * Utiliza MapStruct para gerar as implementações automaticamente.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Converte ExternalProductDto para entidade Product.
     */
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "stringToLocalDateTime")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "stringToLocalDateTime")
    Product toDomain(ExternalProductDto dto);

    /**
     * Converte lista de ExternalProductDto para lista de Product.
     */
    List<Product> toDomainList(List<ExternalProductDto> dtos);

    /**
     * Converte entidade Product para ProductResponse.
     */
    ProductResponse toResponse(Product product);

    /**
     * Converte lista de Product para lista de ProductResponse.
     */
    List<ProductResponse> toResponseList(List<Product> products);

    /**
     * Converte String para LocalDateTime.
     */
    @Named("stringToLocalDateTime")
    default LocalDateTime stringToLocalDateTime(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateString, DATE_FORMATTER);
    }

    /**
     * Converte LocalDateTime para String.
     */
    @Named("localDateTimeToString")
    default String localDateTimeToString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATE_FORMATTER);
    }
}
