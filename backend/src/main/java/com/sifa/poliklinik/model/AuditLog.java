package com.sifa.poliklinik.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String islem; // Yapılan işlem (RANDEVU_OLUSTURMA, RANDEVU_IPTAL, ODEME_TAHSILAT, HASTA_SILME)

    @Column(name = "kullanici_email")
    private String kullaniciEmail; // İşlemi yapan kullanıcı

    @Column(name = "etkilenen_kayit_id")
    private Long etkilenenKayitId; // Etkilenen kaydın ID'si

    @Column(length = 500)
    private String aciklama; // Detaylı açıklama

    @Column(nullable = false)
    private LocalDateTime tarih;

    @PrePersist
    protected void onCreate() {
        if (this.tarih == null) this.tarih = LocalDateTime.now();
    }
}
