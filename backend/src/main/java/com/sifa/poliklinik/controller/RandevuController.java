package com.sifa.poliklinik.controller;

import com.sifa.poliklinik.dto.AlternatifTarihResponse;
import com.sifa.poliklinik.dto.KlinikMusaitlikResponse;
import com.sifa.poliklinik.dto.RandevuRequest;
import com.sifa.poliklinik.model.Randevu;
import com.sifa.poliklinik.model.enums.RandevuDurumu;
import com.sifa.poliklinik.service.RandevuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Randevu yönetimi controller'ı.
 */
@Tag(name = "Randevular", description = "Randevu yönetimi")
@RestController
@RequestMapping("/api/randevular")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class RandevuController {

    private final RandevuService randevuService;

    @Operation(summary = "Tüm randevuları listele")
    @GetMapping
    public ResponseEntity<List<Randevu>> tumRandevulariGetir() {
        return ResponseEntity.ok(randevuService.tumRandevulariGetir());
    }

    @Operation(summary = "ID ile randevu getir")
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Randevu> randevuGetir(@PathVariable Long id) {
        return ResponseEntity.ok(randevuService.randevuGetir(id));
    }

    @GetMapping("/hasta/{hastaId}")
    public ResponseEntity<List<Randevu>> hastaRandevulari(@PathVariable Long hastaId) {
        return ResponseEntity.ok(randevuService.hastaRandevulari(hastaId));
    }

    @GetMapping("/doktor/{doktorId}")
    public ResponseEntity<List<Randevu>> doktorRandevulari(@PathVariable Long doktorId) {
        return ResponseEntity.ok(randevuService.doktorRandevulari(doktorId));
    }

    @GetMapping("/doktor/{doktorId}/bekleyen")
    public ResponseEntity<List<Randevu>> doktorBekleyenRandevular(@PathVariable Long doktorId) {
        return ResponseEntity.ok(randevuService.doktorBekleyenRandevular(doktorId));
    }

    @GetMapping("/musait-saatler")
    public ResponseEntity<List<String>> getMusaitSaatler(
            @RequestParam Long doktorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tarih) {
        return ResponseEntity.ok(randevuService.getMusaitSaatler(doktorId, tarih));
    }

    @Operation(summary = "Yeni randevu oluştur")
    @PostMapping
    @PreAuthorize("hasAnyRole('RANDEVU_GOREVLISI', 'YONETICI')")
    public ResponseEntity<Randevu> randevuOlustur(@Valid @RequestBody RandevuRequest request) {
        return ResponseEntity.ok(randevuService.randevuOlustur(request));
    }

    @Operation(summary = "Randevuyu güncelle")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RANDEVU_GOREVLISI', 'YONETICI')")
    public ResponseEntity<Randevu> randevuGuncelle(@PathVariable Long id, @Valid @RequestBody RandevuRequest request) {
        return ResponseEntity.ok(randevuService.randevuGuncelle(id, request));
    }

    @Operation(summary = "Randevuyu iptal et")
    @PutMapping("/{id}/iptal")
    @PreAuthorize("hasAnyRole('RANDEVU_GOREVLISI', 'YONETICI')")
    public ResponseEntity<Randevu> randevuIptal(@PathVariable Long id) {
        return ResponseEntity.ok(randevuService.randevuIptal(id));
    }

    @PutMapping("/{id}/durum")
    public ResponseEntity<Randevu> durumGuncelle(@PathVariable Long id,
                                                  @RequestParam RandevuDurumu durum) {
        return ResponseEntity.ok(randevuService.durumGuncelle(id, durum));
    }

    @GetMapping("/musait-saatler/klinik")
    public ResponseEntity<List<KlinikMusaitlikResponse>> getKlinikMusaitSaatler(
            @RequestParam Long klinikId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tarih) {
        return ResponseEntity.ok(randevuService.getKlinikMusaitSaatler(klinikId, tarih));
    }

    @GetMapping("/alternatif-tarihler")
    @PreAuthorize("hasAnyRole('RANDEVU_GOREVLISI', 'YONETICI')")
    public ResponseEntity<List<AlternatifTarihResponse>> getAlternatifTarihler(
            @RequestParam Long klinikId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baslangic,
            @RequestParam(defaultValue = "7") int gun) {
        return ResponseEntity.ok(randevuService.getAlternatifTarihler(klinikId, baslangic, gun));
    }
}
