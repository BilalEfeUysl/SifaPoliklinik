package com.sifa.poliklinik.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class KlinikMusaitlikResponse {
    private Long doktorId;
    private String doktorAd;
    private List<String> musaitSaatler;
}
