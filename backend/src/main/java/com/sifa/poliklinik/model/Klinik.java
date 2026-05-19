package com.sifa.poliklinik.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Klinik entity'si.
 * Göz, Üroloji, Ortopedi, Psikiyatri klinikleri.
 */
@Entity
@Table(name = "klinikler")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Klinik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Klinik adı boş olamaz")
    @Column(nullable = false, unique = true)
    private String ad;

    private String aciklama;

    @Column(name = "muayene_ucreti")
    private Double muayeneUcreti;

    @OneToMany(mappedBy = "klinik", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<Doktor> doktorlar = new ArrayList<>();
}
