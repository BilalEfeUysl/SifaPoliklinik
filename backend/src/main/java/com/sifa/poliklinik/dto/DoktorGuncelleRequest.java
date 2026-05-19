package com.sifa.poliklinik.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoktorGuncelleRequest {
    @NotBlank(message = "Ünvan boş olamaz")
    private String unvan;

    @NotBlank(message = "Uzmanlık alanı boş olamaz")
    private String uzmanlikAlani;

    @NotNull(message = "Klinik seçilmelidir")
    private Long klinikId;

    private String sifre;
}
