package com.sifa.poliklinik.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sifa.poliklinik.model.enums.RandevuDurumu;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Randevu entity'si.
 * Hasta ile doktor arasındaki randevu kaydı.
 */
@Entity
@Table(name = "randevular", uniqueConstraints = {
    @UniqueConstraint(name = "uq_doktor_tarihsaat", columnNames = {"doktor_id", "tarih_saat"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Randevu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hasta_id", nullable = false)
    private Hasta hasta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doktor_id", nullable = false)
    private Doktor doktor;

    @Column(name = "tarih_saat", nullable = false)
    private LocalDateTime tarihSaat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RandevuDurumu durum = RandevuDurumu.BEKLIYOR;

    private String notlar;

    @Column(name = "olusturma_tarihi")
    private LocalDateTime olusturmaTarihi;

    @OneToOne(mappedBy = "randevu", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private MuayeneKaydi muayeneKaydi;

    @PrePersist
    protected void onCreate() {
        this.olusturmaTarihi = LocalDateTime.now();
    }
}
