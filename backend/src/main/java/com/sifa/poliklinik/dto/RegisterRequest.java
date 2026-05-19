package com.sifa.poliklinik.dto;

import com.sifa.poliklinik.model.enums.Rol;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class RegisterRequest {
    @NotBlank(message = "Ad boş olamaz")
    private String ad;

    @NotBlank(message = "Soyad boş olamaz")
    private String soyad;

    @NotBlank(message = "Email boş olamaz")
    @Email(message = "Geçerli bir email adresi giriniz")
    private String email;

    @NotBlank(message = "Şifre boş olamaz")
    private String sifre;

    @NotNull(message = "Rol boş olamaz")
    private Rol rol;

    private String telefon;
}
