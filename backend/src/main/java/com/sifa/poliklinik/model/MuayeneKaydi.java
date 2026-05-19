package com.sifa.poliklinik.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Muayene kaydı entity'si.
 * Doktorun muayene sonrası girdiği tanı, reçete ve rapor bilgileri.
 */
@Entity
@Table(name = "muayene_kayitlari")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MuayeneKaydi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "randevu_id", nullable = false, unique = true)
    private Randevu randevu;

    @Column(columnDefinition = "TEXT")
    private String tani;        // Teşhis / tanı

    @Column(columnDefinition = "TEXT")
    private String recete;      // Yazılan ilaçlar

    @Column(columnDefinition = "TEXT")
    private String rapor;       // Muayene raporu

    @Column(name = "sevk_kurumu")
    private String sevkKurumu;  // Sevk edilecek kurum (varsa)

    @Column(name = "muayene_tarihi")
    private LocalDateTime muayeneTarihi;

    @OneToOne(mappedBy = "muayeneKaydi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Odeme odeme;

    @PrePersist
    protected void onCreate() {
        this.muayeneTarihi = LocalDateTime.now();
    }
}
