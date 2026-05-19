package com.sifa.poliklinik.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sifa.poliklinik.model.enums.SGKDurumu;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Hasta entity'si.
 * Poliklinik hastalarının demografik ve sigorta bilgileri.
 */
@Entity
@Table(name = "hastalar")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Hasta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "TC Kimlik numarası boş olamaz")
    @Pattern(regexp = "^[0-9]{11}$", message = "TC Kimlik numarası 11 haneli olmalıdır")
    @Column(name = "tc_kimlik", nullable = false, unique = true, length = 11)
    private String tcKimlik;

    @NotBlank(message = "Ad boş olamaz")
    @Column(nullable = false)
    private String ad;

    @NotBlank(message = "Soyad boş olamaz")
    @Column(nullable = false)
    private String soyad;

    @Column(name = "dogum_tarihi")
    private LocalDate dogumTarihi;

    private String telefon;

    private String adres;

    @Enumerated(EnumType.STRING)
    @Column(name = "sgk_durumu")
    private SGKDurumu sgkDurumu;

    @Column(name = "kayit_tarihi")
    private LocalDateTime kayitTarihi;

    @OneToMany(mappedBy = "hasta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<Randevu> randevular = new ArrayList<>();

    @OneToMany(mappedBy = "hasta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<Odeme> odemeler = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.kayitTarihi = LocalDateTime.now();
    }

    /**
     * Hastanın tam adını döner.
     */
    public String getFullName() {
        return this.ad + " " + this.soyad;
    }
}
