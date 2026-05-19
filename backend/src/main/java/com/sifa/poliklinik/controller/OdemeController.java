package com.sifa.poliklinik.controller;

import com.sifa.poliklinik.dto.TahsilatRequest;
import com.sifa.poliklinik.model.Odeme;
import com.sifa.poliklinik.service.OdemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Ödeme controller'ı.
 * Veznedar erişimine açık.
 */
@Tag(name = "Ödemeler", description = "Ödeme yönetimi")
@RestController
@RequestMapping("/api/odemeler")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class OdemeController {

    private final OdemeService odemeService;

    @Operation(summary = "Tüm ödemeleri listele")
    @GetMapping
    public ResponseEntity<List<Odeme>> tumOdemeleriGetir() {
        return ResponseEntity.ok(odemeService.tumOdemeleriGetir());
    }

    @Operation(summary = "ID ile ödeme getir")
    @GetMapping("/{id}")
    public ResponseEntity<Odeme> odemeGetir(@PathVariable Long id) {
        return ResponseEntity.ok(odemeService.odemeGetir(id));
    }

    @GetMapping("/hasta/{hastaId}")
    public ResponseEntity<List<Odeme>> hastaOdemeleri(@PathVariable Long hastaId) {
        return ResponseEntity.ok(odemeService.hastaOdemeleri(hastaId));
    }

    @GetMapping("/bekleyen")
    public ResponseEntity<List<Odeme>> bekleyenOdemeler() {
        return ResponseEntity.ok(odemeService.bekleyenOdemeler());
    }

    @Operation(summary = "Muayene için ödeme kaydı oluştur")
    @PostMapping("/muayene/{muayeneId}")

    public ResponseEntity<Odeme> odemeOlustur(@PathVariable Long muayeneId) {
        return ResponseEntity.ok(odemeService.odemeOlustur(muayeneId));
    }

    @Operation(summary = "Ödeme tahsilatını tamamla")
    @PutMapping("/{id}/tahsilat")

    public ResponseEntity<Odeme> odemeTahsilat(@PathVariable Long id, @RequestBody TahsilatRequest request) {
        return ResponseEntity.ok(odemeService.odemeTahsilat(id, request.getOdemeTipi()));
    }

    @PostMapping("/{id}/sgk-sorgula")

    public ResponseEntity<Odeme> sgkYenidenSorgula(@PathVariable Long id) {
        return ResponseEntity.ok(odemeService.sgkYenidenSorgula(id));
    }

    @PutMapping("/{id}/iptal")

    public ResponseEntity<Odeme> odemeIptal(@PathVariable Long id) {
        return ResponseEntity.ok(odemeService.odemeIptal(id));
    }
}
