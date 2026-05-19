package com.sifa.poliklinik.controller;

import com.sifa.poliklinik.dto.DoktorGuncelleRequest;
import com.sifa.poliklinik.dto.DoktorOlusturmaRequest;
import com.sifa.poliklinik.dto.RegisterRequest;
import com.sifa.poliklinik.exception.BusinessRuleException;
import com.sifa.poliklinik.exception.ConflictException;
import com.sifa.poliklinik.exception.NotFoundException;
import com.sifa.poliklinik.model.Doktor;
import com.sifa.poliklinik.model.Klinik;
import com.sifa.poliklinik.model.Kullanici;
import com.sifa.poliklinik.model.enums.Rol;
import com.sifa.poliklinik.repository.DoktorRepository;
import com.sifa.poliklinik.repository.KlinikRepository;
import com.sifa.poliklinik.repository.KullaniciRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Yönetici controller'ı.
 * Klinik, doktor ve kullanıcı yönetimi.
 */
@Tag(name = "Yönetici", description = "Klinik, doktor ve kullanıcı yönetimi")
@RestController
@RequestMapping("/api/yonetici")
@RequiredArgsConstructor
@PreAuthorize("hasRole('YONETICI')")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class YoneticiController {

    private final KlinikRepository klinikRepository;
    private final DoktorRepository doktorRepository;
    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;

    // ---- Klinik Yönetimi ----

    @Operation(summary = "Tüm klinikleri listele")
    @GetMapping("/klinikler")
    public ResponseEntity<List<Klinik>> tumKlinikler() {
        return ResponseEntity.ok(klinikRepository.findAll());
    }

    @Operation(summary = "Yeni klinik ekle")
    @PostMapping("/klinikler")
    public ResponseEntity<Klinik> klinikEkle(@RequestBody Klinik klinik) {
        return ResponseEntity.ok(klinikRepository.save(klinik));
    }

    @PutMapping("/klinikler/{id}")
    public ResponseEntity<Klinik> klinikGuncelle(@PathVariable Long id, @RequestBody Klinik request) {
        Klinik klinik = klinikRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Klinik bulunamadı: ID " + id));
        klinik.setAd(request.getAd());
        klinik.setAciklama(request.getAciklama());
        klinik.setMuayeneUcreti(request.getMuayeneUcreti());
        return ResponseEntity.ok(klinikRepository.save(klinik));
    }

    @DeleteMapping("/klinikler/{id}")
    public ResponseEntity<Void> klinikSil(@PathVariable Long id) {
        Klinik klinik = klinikRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Klinik bulunamadı: ID " + id));
        if (!klinik.getDoktorlar().isEmpty()) {
            throw new BusinessRuleException("Klinikte kayıtlı doktor var, önce doktorları silin veya taşıyın.");
        }
        klinikRepository.delete(klinik);
        return ResponseEntity.noContent().build();
    }

    // ---- Doktor Yönetimi ----

    @Operation(summary = "Tüm doktorları listele")
    @GetMapping("/doktorlar")
    public ResponseEntity<List<Doktor>> tumDoktorlar() {
        return ResponseEntity.ok(doktorRepository.findAll());
    }

    @Operation(summary = "Yeni doktor ekle")
    @PostMapping("/doktorlar")
    @Transactional
    public ResponseEntity<Doktor> doktorEkle(@Valid @RequestBody DoktorOlusturmaRequest request) {
        if (kullaniciRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Bu email zaten kullanımda: " + request.getEmail());
        }

        Kullanici kullanici = Kullanici.builder()
                .ad(request.getAd())
                .soyad(request.getSoyad())
                .email(request.getEmail())
                .sifre(passwordEncoder.encode(request.getSifre()))
                .rol(Rol.DOKTOR)
                .telefon(request.getTelefon())
                .build();
        kullanici = kullaniciRepository.save(kullanici);

        Klinik klinik = klinikRepository.findById(request.getKlinikId())
                .orElseThrow(() -> new NotFoundException("Klinik bulunamadı"));

        Doktor doktor = Doktor.builder()
                .kullanici(kullanici)
                .klinik(klinik)
                .unvan(request.getUnvan())
                .uzmanlikAlani(request.getUzmanlikAlani())
                .build();

        return ResponseEntity.ok(doktorRepository.save(doktor));
    }

    @PutMapping("/doktorlar/{id}/musaitlik")
    @Operation(summary = "Doktor müsaitlik durumunu değiştir")
    public ResponseEntity<Doktor> doktorMusaitlikToggle(@PathVariable Long id) {
        Doktor doktor = doktorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Doktor bulunamadı: ID " + id));
        doktor.setMusaitMi(!doktor.isMusaitMi());
        return ResponseEntity.ok(doktorRepository.save(doktor));
    }

    @PutMapping("/doktorlar/{id}")
    public ResponseEntity<Doktor> doktorGuncelle(@PathVariable Long id,
            @Valid @RequestBody DoktorGuncelleRequest request) {
        Doktor doktor = doktorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Doktor bulunamadı: ID " + id));
        Klinik klinik = klinikRepository.findById(request.getKlinikId())
                .orElseThrow(() -> new NotFoundException("Klinik bulunamadı"));
        doktor.setUnvan(request.getUnvan());
        doktor.setUzmanlikAlani(request.getUzmanlikAlani());
        doktor.setKlinik(klinik);
        if (request.getSifre() != null && !request.getSifre().isBlank()) {
            doktor.getKullanici().setSifre(passwordEncoder.encode(request.getSifre()));
            kullaniciRepository.save(doktor.getKullanici());
        }
        return ResponseEntity.ok(doktorRepository.save(doktor));
    }

    @DeleteMapping("/doktorlar/{id}")
    @Transactional
    public ResponseEntity<Void> doktorSil(@PathVariable Long id) {
        Doktor doktor = doktorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Doktor bulunamadı: ID " + id));
        Long kullaniciId = doktor.getKullanici().getId();
        doktorRepository.delete(doktor);
        kullaniciRepository.deleteById(kullaniciId);
        return ResponseEntity.noContent().build();
    }

    // ---- Kullanıcı Yönetimi ----

    @Operation(summary = "Tüm kullanıcıları listele")
    @GetMapping("/kullanicilar")
    public ResponseEntity<List<Kullanici>> tumKullanicilar() {
        return ResponseEntity.ok(kullaniciRepository.findAll());
    }

    // ---- Görevli Yönetimi ----

    @PostMapping("/kullanicilar")
    public ResponseEntity<Kullanici> gorevliEkle(@Valid @RequestBody RegisterRequest request) {
        if (request.getRol() == Rol.DOKTOR) {
            throw new BusinessRuleException("Doktor eklemek için /yonetici/doktorlar endpoint'ini kullanın.");
        }
        if (kullaniciRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Bu email zaten kullanımda: " + request.getEmail());
        }
        Kullanici kullanici = Kullanici.builder()
                .ad(request.getAd())
                .soyad(request.getSoyad())
                .email(request.getEmail())
                .sifre(passwordEncoder.encode(request.getSifre()))
                .rol(request.getRol())
                .telefon(request.getTelefon())
                .build();
        return ResponseEntity.ok(kullaniciRepository.save(kullanici));
    }

    @PutMapping("/kullanicilar/{id}")
    public ResponseEntity<Kullanici> gorevliGuncelle(@PathVariable Long id,
            @RequestBody RegisterRequest request) {
        Kullanici kullanici = kullaniciRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı: ID " + id));
        if (kullanici.getRol() == Rol.DOKTOR) {
            throw new BusinessRuleException("Doktor bilgilerini güncellemek için /yonetici/doktorlar endpoint'ini kullanın.");
        }
        if (request.getRol() == Rol.DOKTOR) {
            throw new BusinessRuleException("Bu endpoint üzerinden DOKTOR rolü atanamaz.");
        }
        kullanici.setAd(request.getAd());
        kullanici.setSoyad(request.getSoyad());
        kullanici.setEmail(request.getEmail());
        kullanici.setTelefon(request.getTelefon());
        kullanici.setRol(request.getRol());
        if (request.getSifre() != null && !request.getSifre().isBlank()) {
            kullanici.setSifre(passwordEncoder.encode(request.getSifre()));
        }
        return ResponseEntity.ok(kullaniciRepository.save(kullanici));
    }

    @DeleteMapping("/kullanicilar/{id}")
    public ResponseEntity<Void> gorevliSil(@PathVariable Long id) {
        Kullanici kullanici = kullaniciRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı: ID " + id));
        if (kullanici.getRol() == Rol.DOKTOR) {
            throw new BusinessRuleException("Doktor silmek için /yonetici/doktorlar endpoint'ini kullanın.");
        }
        kullaniciRepository.delete(kullanici);
        return ResponseEntity.noContent().build();
    }

    // ---- Dashboard İstatistikleri ----

    @GetMapping("/istatistikler")
    public ResponseEntity<Map<String, Object>> istatistikler() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("toplamKlinik", klinikRepository.count());
        stats.put("toplamDoktor", doktorRepository.count());
        stats.put("toplamKullanici", kullaniciRepository.count());
        return ResponseEntity.ok(stats);
    }
}
