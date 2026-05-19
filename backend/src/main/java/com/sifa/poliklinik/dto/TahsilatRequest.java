package com.sifa.poliklinik.dto;

import com.sifa.poliklinik.model.enums.OdemeTipi;
import lombok.Data;

@Data
public class TahsilatRequest {
    private OdemeTipi odemeTipi;
}
