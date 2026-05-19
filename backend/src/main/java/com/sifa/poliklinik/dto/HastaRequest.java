package com.sifa.poliklinik.dto;

import com.sifa.poliklinik.model.enums.SGKDurumu;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
public class HastaRequest {
    @NotBlank(message = "TC Kimlik numarası boş olamaz")
    @Pattern(regexp = "^[0-9]{11}$", message = "TC Kimlik numarası 11 haneli olmalıdır")
    private String tcKimlik;

    @NotBlank(message = "Ad boş olamaz")
    private String ad;

    @NotBlank(message = "Soyad boş olamaz")
    private String soyad;

    private LocalDate dogumTarihi;
    private String telefon;
    private String adres;
    private SGKDurumu sgkDurumu;
}
