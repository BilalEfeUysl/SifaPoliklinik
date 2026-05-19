package com.sifa.poliklinik.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoktorOlusturmaRequest {

    @NotBlank(message = "Ad boş olamaz")
    private String ad;

    @NotBlank(message = "Soyad boş olamaz")
    private String soyad;

    @NotBlank(message = "Email boş olamaz")
    @Email(message = "Geçerli bir email adresi giriniz")
    private String email;

    @NotBlank(message = "Şifre boş olamaz")
    private String sifre;

    private String telefon;

    @NotBlank(message = "Ünvan boş olamaz")
    private String unvan;

    @NotBlank(message = "Uzmanlık alanı boş olamaz")
    private String uzmanlikAlani;

    @NotNull(message = "Klinik seçilmelidir")
    private Long klinikId;
}
