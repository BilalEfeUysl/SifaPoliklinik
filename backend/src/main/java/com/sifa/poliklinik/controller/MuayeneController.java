package com.sifa.poliklinik.controller;

import com.sifa.poliklinik.dto.MuayeneRequest;
import com.sifa.poliklinik.model.MuayeneKaydi;
import com.sifa.poliklinik.service.MuayeneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Muayene controller'ı.
 * Doktor erişimine açık.
 */
@Tag(name = "Muayene Kayıtları", description = "Muayene kaydı yönetimi")
@RestController
@RequestMapping("/api/muayeneler")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class MuayeneController {

    private final MuayeneService muayeneService;

    @GetMapping
    @PreAuthorize("hasRole('YONETICI')")
    public ResponseEntity<List<MuayeneKaydi>> tumMuayeneler() {
        return ResponseEntity.ok(muayeneService.tumMuayeneler());
    }

    @Operation(summary = "ID ile muayene kaydı getir")
    @GetMapping("/{id}")
    public ResponseEntity<MuayeneKaydi> muayeneGetir(@PathVariable Long id) {
        return ResponseEntity.ok(muayeneService.muayeneGetir(id));
    }

    @GetMapping("/randevu/{randevuId}")
    public ResponseEntity<MuayeneKaydi> randevuIleMuayeneGetir(@PathVariable Long randevuId) {
        return ResponseEntity.ok(muayeneService.randevuIleMuayeneGetir(randevuId));
    }

    @GetMapping("/hasta/{hastaId}")
    public ResponseEntity<List<MuayeneKaydi>> hastaMuayeneleri(@PathVariable Long hastaId) {
        return ResponseEntity.ok(muayeneService.hastaMuayeneleri(hastaId));
    }

    @GetMapping("/doktor/{doktorId}")
    public ResponseEntity<List<MuayeneKaydi>> doktorMuayeneleri(@PathVariable Long doktorId) {
        return ResponseEntity.ok(muayeneService.doktorMuayeneleri(doktorId));
    }

    @Operation(summary = "Yeni muayene kaydı oluştur")
    @PostMapping
    @PreAuthorize("hasAnyRole('DOKTOR', 'YONETICI')")
    public ResponseEntity<MuayeneKaydi> muayeneKaydet(@Valid @RequestBody MuayeneRequest request) {
        return ResponseEntity.ok(muayeneService.muayeneKaydet(request));
    }

    @Operation(summary = "Muayene kaydını güncelle")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOKTOR', 'YONETICI')")
    public ResponseEntity<MuayeneKaydi> muayeneGuncelle(@PathVariable Long id,
                                                         @Valid @RequestBody MuayeneRequest request) {
        return ResponseEntity.ok(muayeneService.muayeneGuncelle(id, request));
    }
}
