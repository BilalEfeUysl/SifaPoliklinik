package com.sifa.poliklinik.dto;

import com.sifa.poliklinik.model.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long id;
    private String ad;
    private String soyad;
    private String email;
    private Rol rol;
}
