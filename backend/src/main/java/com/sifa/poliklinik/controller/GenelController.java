package com.sifa.poliklinik.controller;

import com.sifa.poliklinik.model.Doktor;
import com.sifa.poliklinik.model.Klinik;
import com.sifa.poliklinik.repository.DoktorRepository;
import com.sifa.poliklinik.repository.KlinikRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Genel listeleme controller'ı.
 * Klinik ve doktor bilgilerini tüm giriş yapmış kullanıcılar görebilir.
 */
@Tag(name = "Genel", description = "Genel sorgular")
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class GenelController {

    private final KlinikRepository klinikRepository;
    private final DoktorRepository doktorRepository;

    @Operation(summary = "Tüm klinikleri listele")
    @GetMapping("/api/klinikler")
    public ResponseEntity<List<Klinik>> tumKlinikler() {
        return ResponseEntity.ok(klinikRepository.findAll());
    }

    @GetMapping("/api/klinikler/{id}")
    public ResponseEntity<Klinik> klinikGetir(@PathVariable Long id) {
        return ResponseEntity.ok(klinikRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Klinik bulunamadı: ID " + id)));
    }

    @Operation(summary = "Tüm doktorları listele")
    @GetMapping("/api/doktorlar")
    public ResponseEntity<List<Doktor>> tumDoktorlar() {
        return ResponseEntity.ok(doktorRepository.findAll());
    }

    @GetMapping("/api/doktorlar/klinik/{klinikId}")
    public ResponseEntity<List<Doktor>> klinikDoktorlari(@PathVariable Long klinikId) {
        return ResponseEntity.ok(doktorRepository.findByKlinikId(klinikId));
    }

    @Operation(summary = "ID ile doktor getir")
    @GetMapping("/api/doktorlar/{id}")
    public ResponseEntity<Doktor> doktorGetir(@PathVariable Long id) {
        return ResponseEntity.ok(doktorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı: ID " + id)));
    }
}
