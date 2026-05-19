package com.sifa.poliklinik.config;

import com.sifa.poliklinik.model.*;
import com.sifa.poliklinik.model.enums.Rol;
import com.sifa.poliklinik.model.enums.SGKDurumu;
import com.sifa.poliklinik.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Uygulama başlangıcında örnek verileri yükler.
 * 4 Klinik, 8 Doktor, 5 Kullanıcı, 5 Hasta.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final KullaniciRepository kullaniciRepository;
    private final KlinikRepository klinikRepository;
    private final DoktorRepository doktorRepository;
    private final HastaRepository hastaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (kullaniciRepository.count() > 0) {
            log.info("Veriler zaten mevcut, seed atlanıyor.");
            return;
        }

        log.info("=== Örnek veriler yükleniyor ===");

        // ---- Klinikler ----
        Klinik goz = klinikRepository.save(Klinik.builder()
                .ad("Göz Hastalıkları").aciklama("Göz muayenesi ve tedavisi").muayeneUcreti(250.0).build());
        Klinik uroloji = klinikRepository.save(Klinik.builder()
                .ad("Üroloji").aciklama("Ürolojik hastalıkların tanı ve tedavisi").muayeneUcreti(300.0).build());
        Klinik ortopedi = klinikRepository.save(Klinik.builder()
                .ad("Ortopedi").aciklama("Kas-iskelet sistemi hastalıkları").muayeneUcreti(350.0).build());
        Klinik psikiyatri = klinikRepository.save(Klinik.builder()
                .ad("Psikiyatri").aciklama("Ruh sağlığı ve tedavisi").muayeneUcreti(200.0).build());

        log.info("4 klinik oluşturuldu.");

        // ---- Sistem Kullanıcıları ----
        Kullanici yonetici = kullaniciRepository.save(Kullanici.builder()
                .ad("Admin").soyad("Yönetici").email("admin@sifa.com")
                .sifre(passwordEncoder.encode("admin123")).rol(Rol.YONETICI).telefon("05001112233").build());

        Kullanici kayitGorevlisi = kullaniciRepository.save(Kullanici.builder()
                .ad("Ayşe").soyad("Demir").email("ayse@sifa.com")
                .sifre(passwordEncoder.encode("kayit123")).rol(Rol.KAYIT_GOREVLISI).telefon("05002223344").build());

        Kullanici randevuGorevlisi = kullaniciRepository.save(Kullanici.builder()
                .ad("Fatma").soyad("Yılmaz").email("fatma@sifa.com")
                .sifre(passwordEncoder.encode("randevu123")).rol(Rol.RANDEVU_GOREVLISI).telefon("05003334455").build());

        Kullanici veznedar = kullaniciRepository.save(Kullanici.builder()
                .ad("Mehmet").soyad("Kara").email("mehmet@sifa.com")
                .sifre(passwordEncoder.encode("vezne123")).rol(Rol.VEZNEDAR).telefon("05004445566").build());

        log.info("4 sistem kullanıcısı oluşturuldu.");

        // ---- Doktor Kullanıcıları & Doktorlar ----
        // Göz Klinik Doktorları
        Kullanici drAhmet = kullaniciRepository.save(Kullanici.builder()
                .ad("Ahmet").soyad("Öztürk").email("ahmet.dr@sifa.com")
                .sifre(passwordEncoder.encode("doktor123")).rol(Rol.DOKTOR).telefon("05301112233").build());
        doktorRepository.save(Doktor.builder()
                .kullanici(drAhmet).klinik(goz).unvan("Uzm. Dr.").uzmanlikAlani("Göz Hastalıkları").build());

        Kullanici drZeynep = kullaniciRepository.save(Kullanici.builder()
                .ad("Zeynep").soyad("Aksoy").email("zeynep.dr@sifa.com")
                .sifre(passwordEncoder.encode("doktor123")).rol(Rol.DOKTOR).telefon("05302223344").build());
        doktorRepository.save(Doktor.builder()
                .kullanici(drZeynep).klinik(goz).unvan("Dr.").uzmanlikAlani("Retina Cerrahisi").build());

        // Üroloji Doktorları
        Kullanici drAli = kullaniciRepository.save(Kullanici.builder()
                .ad("Ali").soyad("Yıldız").email("ali.dr@sifa.com")
                .sifre(passwordEncoder.encode("doktor123")).rol(Rol.DOKTOR).telefon("05303334455").build());
        doktorRepository.save(Doktor.builder()
                .kullanici(drAli).klinik(uroloji).unvan("Prof. Dr.").uzmanlikAlani("Üroloji").build());

        // Ortopedi Doktorları
        Kullanici drCan = kullaniciRepository.save(Kullanici.builder()
                .ad("Can").soyad("Şahin").email("can.dr@sifa.com")
                .sifre(passwordEncoder.encode("doktor123")).rol(Rol.DOKTOR).telefon("05304445566").build());
        doktorRepository.save(Doktor.builder()
                .kullanici(drCan).klinik(ortopedi).unvan("Doç. Dr.").uzmanlikAlani("Ortopedi ve Travmatoloji").build());

        // Psikiyatri Doktorları
        Kullanici drEmine = kullaniciRepository.save(Kullanici.builder()
                .ad("Emine").soyad("Çelik").email("emine.dr@sifa.com")
                .sifre(passwordEncoder.encode("doktor123")).rol(Rol.DOKTOR).telefon("05305556677").build());
        doktorRepository.save(Doktor.builder()
                .kullanici(drEmine).klinik(psikiyatri).unvan("Uzm. Dr.").uzmanlikAlani("Psikiyatri").build());

        log.info("5 doktor oluşturuldu.");

        // ---- Örnek Hastalar ----
        hastaRepository.save(Hasta.builder()
                .tcKimlik("12345678900").ad("Hasan").soyad("Kaya")
                .dogumTarihi(LocalDate.of(1985, 3, 15)).telefon("05551112233")
                .adres("İstanbul, Kadıköy").sgkDurumu(SGKDurumu.AKTIF).build());

        hastaRepository.save(Hasta.builder()
                .tcKimlik("12345678901").ad("Elif").soyad("Arslan")
                .dogumTarihi(LocalDate.of(1990, 7, 22)).telefon("05552223344")
                .adres("Ankara, Çankaya").sgkDurumu(SGKDurumu.PASIF).build());

        hastaRepository.save(Hasta.builder()
                .tcKimlik("12345678902").ad("Murat").soyad("Aydın")
                .dogumTarihi(LocalDate.of(1978, 11, 5)).telefon("05553334455")
                .adres("İzmir, Bornova").sgkDurumu(SGKDurumu.AKTIF).build());

        hastaRepository.save(Hasta.builder()
                .tcKimlik("12345678904").ad("Selin").soyad("Güneş")
                .dogumTarihi(LocalDate.of(1995, 1, 30)).telefon("05554445566")
                .adres("Bursa, Nilüfer").sgkDurumu(SGKDurumu.AKTIF).build());

        hastaRepository.save(Hasta.builder()
                .tcKimlik("12345678903").ad("Burak").soyad("Koç")
                .dogumTarihi(LocalDate.of(2000, 6, 10)).telefon("05555556677")
                .adres("Antalya, Muratpaşa").sgkDurumu(SGKDurumu.PASIF).build());

        log.info("5 hasta oluşturuldu.");
        log.info("=== Örnek veri yükleme tamamlandı ===");
        log.info("");
        log.info("Giriş Bilgileri:");
        log.info("  Yönetici:          admin@sifa.com / admin123");
        log.info("  Kayıt Görevlisi:   ayse@sifa.com / kayit123");
        log.info("  Randevu Görevlisi: fatma@sifa.com / randevu123");
        log.info("  Veznedar:          mehmet@sifa.com / vezne123");
        log.info("  Doktor (Göz):      ahmet.dr@sifa.com / doktor123");
    }
}
