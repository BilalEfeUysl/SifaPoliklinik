package com.sifa.poliklinik.controller;

import com.sifa.poliklinik.dto.HastaRequest;
import com.sifa.poliklinik.model.Hasta;
import com.sifa.poliklinik.service.HastaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hasta yönetimi controller'ı.
 * Kayıt Görevlisi ve Yönetici erişimine açık.
 */
@Tag(name = "Hastalar", description = "Hasta yönetimi")
@RestController
@RequestMapping("/api/hastalar")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class HastaController {

    private final HastaService hastaService;

    @Operation(summary = "Tüm hastaları listele (sayfalama destekli)")
    @GetMapping
    public ResponseEntity<?> tumHastalariGetir(
            @RequestParam(defaultValue = "-1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0) {
            // Legacy: no pagination, return full list
            return ResponseEntity.ok(hastaService.tumHastalariGetir());
        }
        return ResponseEntity.ok(hastaService.tumHastalariGetirSayfalama(page, size));
    }

    @Operation(summary = "ID ile hasta getir")
    @GetMapping("/{id}")
    public ResponseEntity<Hasta> hastaGetir(@PathVariable Long id) {
        return ResponseEntity.ok(hastaService.hastaGetir(id));
    }

    @GetMapping("/tc/{tcKimlik}")
    public ResponseEntity<Hasta> hastaTcIleGetir(@PathVariable String tcKimlik) {
        return ResponseEntity.ok(hastaService.hastaTcIleGetir(tcKimlik));
    }

    @GetMapping("/ara")
    public ResponseEntity<List<Hasta>> hastaAra(@RequestParam String q) {
        return ResponseEntity.ok(hastaService.hastaAra(q));
    }

    @Operation(summary = "Yeni hasta kaydet")
    @PostMapping
    @PreAuthorize("hasAnyRole('KAYIT_GOREVLISI', 'YONETICI')")
    public ResponseEntity<Hasta> hastaKaydet(@Valid @RequestBody HastaRequest request) {
        return ResponseEntity.ok(hastaService.hastaKaydet(request));
    }

    @Operation(summary = "Hasta bilgilerini güncelle")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('KAYIT_GOREVLISI', 'YONETICI')")
    public ResponseEntity<Hasta> hastaGuncelle(@PathVariable Long id,
                                                @Valid @RequestBody HastaRequest request) {
        return ResponseEntity.ok(hastaService.hastaGuncelle(id, request));
    }

    @Operation(summary = "Hasta sil")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('KAYIT_GOREVLISI', 'YONETICI')")
    public ResponseEntity<Void> hastaSil(@PathVariable Long id) {
        hastaService.hastaSil(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/borc")
    public ResponseEntity<Map<String, Object>> borcKontrol(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("hastaId", id);
        result.put("toplamBorc", hastaService.borcKontrol(id));
        result.put("borcluMu", hastaService.borcluMu(id));
        return ResponseEntity.ok(result);
    }
}
