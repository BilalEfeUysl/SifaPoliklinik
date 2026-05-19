package com.sifa.poliklinik.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Data
public class MuayeneRequest {
    @NotNull(message = "Randevu ID boş olamaz")
    private Long randevuId;

    @NotBlank(message = "Tanı boş olamaz")
    private String tani;

    private String recete;
    private String rapor;
    private String sevkKurumu;
}
