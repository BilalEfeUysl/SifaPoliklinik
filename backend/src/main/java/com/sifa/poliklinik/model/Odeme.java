package com.sifa.poliklinik.model;

import com.sifa.poliklinik.model.enums.OdemeDurumu;
import com.sifa.poliklinik.model.enums.OdemeTipi;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Ödeme entity'si.
 * Muayene sonrası oluşan ücret ve tahsilat bilgileri.
 * SGK aktif ise %80 indirim uygulanır.
 */
@Entity
@Table(name = "odemeler")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Odeme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hasta_id", nullable = false)
    private Hasta hasta;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "muayene_id", unique = true)
    private MuayeneKaydi muayeneKaydi;

    @Column(name = "toplam_tutar", nullable = false)
    private Double toplamTutar;       // Klinik muayene ücreti

    @Column(name = "sgk_indirimi")
    @Builder.Default
    private Double sgkIndirimi = 0.0; // SGK indirimi (aktif ise %80)

    @Column(name = "net_tutar", nullable = false)
    private Double netTutar;          // Hastanın ödeyeceği tutar

    @Enumerated(EnumType.STRING)
    @Column(name = "odeme_durumu", nullable = false)
    @Builder.Default
    private OdemeDurumu odemeDurumu = OdemeDurumu.BEKLIYOR;

    @Enumerated(EnumType.STRING)
    @Column(name = "odeme_tipi")
    private OdemeTipi odemeTipi;

    @Column(name = "sgk_aktif")
    private Boolean sgkAktif;

    @Column(name = "odeme_tarihi")
    private LocalDateTime odemeTarihi;

    @Column(name = "olusturma_tarihi")
    private LocalDateTime olusturmaTarihi;

    @PrePersist
    protected void onCreate() {
        this.olusturmaTarihi = LocalDateTime.now();
    }
}
