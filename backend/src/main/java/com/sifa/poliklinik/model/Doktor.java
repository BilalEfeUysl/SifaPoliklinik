package com.sifa.poliklinik.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Doktor entity'si.
 * Her doktor bir kliniğe bağlıdır ve bir kullanıcı hesabına sahiptir.
 */
@Entity
@Table(name = "doktorlar")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Doktor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ünvan boş olamaz")
    @Column(nullable = false)
    private String unvan; // Dr., Uzm. Dr., Prof. Dr., vb.

    @NotBlank(message = "Uzmanlık alanı boş olamaz")
    @Column(name = "uzmanlik_alani", nullable = false)
    private String uzmanlikAlani;

    @Builder.Default
    @Column(nullable = false)
    private boolean musaitMi = true;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "kullanici_id", nullable = false, unique = true)
    private Kullanici kullanici;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "klinik_id", nullable = false)
    private Klinik klinik;

    @OneToMany(mappedBy = "doktor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<Randevu> randevular = new ArrayList<>();

    /**
     * Doktorun tam adını ve ünvanını döner.
     */
    public String getFullName() {
        return this.unvan + " " + kullanici.getAd() + " " + kullanici.getSoyad();
    }
}
