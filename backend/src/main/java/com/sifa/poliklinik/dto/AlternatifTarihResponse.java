package com.sifa.poliklinik.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AlternatifTarihResponse {
    private Long doktorId;
    private String doktorAd;
    private LocalDateTime tarihSaat;
}
