package com.sifa.poliklinik.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class RandevuRequest {
    @NotNull(message = "Hasta ID boş olamaz")
    private Long hastaId;

    @NotNull(message = "Doktor ID boş olamaz")
    private Long doktorId;

    @NotNull(message = "Randevu tarih/saati boş olamaz")
    private LocalDateTime tarihSaat;

    private String notlar;
}
