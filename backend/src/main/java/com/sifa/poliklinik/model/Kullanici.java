package com.sifa.poliklinik.model;

import com.sifa.poliklinik.model.enums.Rol;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Sistem kullanıcısı entity'si.
 * Tüm roller (Kayıt Görevlisi, Randevu Görevlisi, Doktor, Veznedar, Yönetici)
 * bu tablo üzerinden yönetilir.
 */
@Entity
@Table(name = "kullanicilar")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Kullanici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ad boş olamaz")
    @Column(nullable = false)
    private String ad;

    @NotBlank(message = "Soyad boş olamaz")
    @Column(nullable = false)
    private String soyad;

    @NotBlank(message = "Email boş olamaz")
    @Email(message = "Geçerli bir email adresi giriniz")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Şifre boş olamaz")
    @Column(nullable = false)
    private String sifre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    private String telefon;

    @Column(name = "olusturma_tarihi")
    private LocalDateTime olusturmaTarihi;

    @PrePersist
    protected void onCreate() {
        this.olusturmaTarihi = LocalDateTime.now();
    }

    /**
     * Kullanıcının tam adını döner.
     */
    public String getFullName() {
        return this.ad + " " + this.soyad;
    }
}
