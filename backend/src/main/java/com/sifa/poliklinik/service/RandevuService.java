package com.sifa.poliklinik.service;

import com.sifa.poliklinik.dto.AlternatifTarihResponse;
import com.sifa.poliklinik.dto.KlinikMusaitlikResponse;
import com.sifa.poliklinik.dto.RandevuRequest;
import com.sifa.poliklinik.exception.BusinessRuleException;
import com.sifa.poliklinik.exception.ConflictException;
import com.sifa.poliklinik.exception.NotFoundException;
import com.sifa.poliklinik.model.Doktor;
import com.sifa.poliklinik.model.Hasta;
import com.sifa.poliklinik.model.Randevu;
import com.sifa.poliklinik.model.enums.RandevuDurumu;
import com.sifa.poliklinik.repository.DoktorRepository;
import com.sifa.poliklinik.repository.HastaRepository;
import com.sifa.poliklinik.repository.RandevuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Randevu yönetimi servisi.
 * İş kuralları: borç kontrolü ve çakışma kontrolü.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RandevuService {

    private final RandevuRepository randevuRepository;
    private final HastaRepository hastaRepository;
    private final DoktorRepository doktorRepository;
    private final HastaService hastaService;
    private final AuditLogService auditLogService;

    @Value("${poliklinik.randevu.calisma-baslangic-saati:9}")
    private int calismaBaslangicSaati;

    @Value("${poliklinik.randevu.calisma-bitis-saati:17}")
    private int calismaBitisSaati;

    @Value("${poliklinik.randevu.ogle-molasi-saati:12}")
    private int ogleMolasiSaati;

    /**
     * Yeni randevu oluşturur.
     * İş Kuralı 1: Borçlu hastaya randevu verilemez.
     * İş Kuralı 2: Aynı doktora aynı saatte randevu çakışması olamaz.
     */
    public Randevu randevuOlustur(RandevuRequest request) {
        // Hasta kontrolü
        Hasta hasta = hastaRepository.findById(request.getHastaId())
                .orElseThrow(() -> new NotFoundException("Hasta bulunamadı: ID " + request.getHastaId()));

        // İş Kuralı 1: Borç kontrolü
        if (hastaService.borcluMu(request.getHastaId())) {
            throw new BusinessRuleException(
                "RANDEVU REDDEDİLDİ: Hasta " + hasta.getFullName() +
                " (TC: " + hasta.getTcKimlik() + ") borçludur. " +
                "Önce ödenmemiş borçların tahsil edilmesi gerekmektedir."
            );
        }

        // Doktor kontrolü
        Doktor doktor = doktorRepository.findById(request.getDoktorId())
                .orElseThrow(() -> new NotFoundException("Doktor bulunamadı: ID " + request.getDoktorId()));

        // İş Kuralı: Müsait olmayan doktora randevu verilemez
        if (!doktor.isMusaitMi()) {
            throw new BusinessRuleException(
                doktor.getFullName() + " şu an randevu kabul etmemektedir."
            );
        }

        LocalDateTime tarihSaat = request.getTarihSaat();
        int saat = tarihSaat.getHour();
        int dakika = tarihSaat.getMinute();

        // İş Kuralı: Randevular yalnızca 09:00 - 17:00 arası (son randevu 16:30)
        if (saat < calismaBaslangicSaati || saat >= calismaBitisSaati) {
            throw new BusinessRuleException("Randevular yalnızca 09:00 ile 17:00 arasında verilebilir.");
        }

        // İş Kuralı: 12:00 - 13:00 arası yemek molası
        if (saat == ogleMolasiSaati) {
            throw new BusinessRuleException("12:00 - 13:00 saatleri arası öğle molasıdır.");
        }

        // İş Kuralı: 30 dakikalık periyotlar
        if (dakika != 0 && dakika != 30) {
            throw new BusinessRuleException("Randevular 30 dakikalık periyotlar (00 veya 30 geçe) halinde verilebilir.");
        }

        // İş Kuralı 2: Çakışma kontrolü (tam saat eşleşmesi)
        if (randevuRepository.existsByDoktorIdAndTarihSaatAndDurumNot(
                request.getDoktorId(), request.getTarihSaat(), RandevuDurumu.IPTAL)) {
            throw new ConflictException(
                "RANDEVU ÇAKIŞMASI: " + doktor.getFullName() +
                " için " + request.getTarihSaat() + " saatinde başka randevu bulunmaktadır."
            );
        }

        Randevu randevu = Randevu.builder()
                .hasta(hasta)
                .doktor(doktor)
                .tarihSaat(request.getTarihSaat())
                .durum(RandevuDurumu.BEKLIYOR)
                .notlar(request.getNotlar())
                .build();

        try {
            Randevu kaydedilenRandevu = randevuRepository.save(randevu);
            log.info("Randevu oluşturuldu: Hasta ID={}, Doktor ID={}, Tarih={}", hasta.getId(), doktor.getId(), request.getTarihSaat());
            auditLogService.kaydet("RANDEVU_OLUSTURMA", "sistem", kaydedilenRandevu.getId(),
                "Hasta: " + hasta.getFullName() + ", Doktor: " + doktor.getFullName() + ", Tarih: " + request.getTarihSaat());
            return kaydedilenRandevu;
        } catch (DataIntegrityViolationException e) {
            log.warn("Randevu çakışması (DB constraint): DoktorID={}, Tarih={}", request.getDoktorId(), request.getTarihSaat());
            throw new ConflictException(
                "RANDEVU ÇAKIŞMASI: " + doktor.getFullName() +
                " için " + request.getTarihSaat() + " saatinde başka randevu bulunmaktadır."
            );
        }
    }

    public Randevu randevuGuncelle(Long id, RandevuRequest request) {
        Randevu mevcutRandevu = randevuGetir(id);

        if (mevcutRandevu.getDurum() != RandevuDurumu.BEKLIYOR) {
            throw new BusinessRuleException("Sadece bekleyen randevular düzenlenebilir.");
        }

        Hasta hasta = hastaRepository.findById(request.getHastaId())
                .orElseThrow(() -> new NotFoundException("Hasta bulunamadı: ID " + request.getHastaId()));

        if (hastaService.borcluMu(request.getHastaId())) {
            throw new BusinessRuleException(
                "RANDEVU REDDEDİLDİ: Hasta " + hasta.getFullName() + " borçludur."
            );
        }

        Doktor doktor = doktorRepository.findById(request.getDoktorId())
                .orElseThrow(() -> new NotFoundException("Doktor bulunamadı: ID " + request.getDoktorId()));

        if (!doktor.isMusaitMi()) {
            throw new BusinessRuleException(doktor.getFullName() + " şu an randevu kabul etmemektedir.");
        }

        LocalDateTime tarihSaat = request.getTarihSaat();
        int saat = tarihSaat.getHour();
        int dakika = tarihSaat.getMinute();

        if (saat < calismaBaslangicSaati || saat >= calismaBitisSaati) {
            throw new BusinessRuleException("Randevular yalnızca 09:00 ile 17:00 arasında verilebilir.");
        }

        if (saat == ogleMolasiSaati) {
            throw new BusinessRuleException("12:00 - 13:00 saatleri arası öğle molasıdır.");
        }

        if (dakika != 0 && dakika != 30) {
            throw new BusinessRuleException("Randevular 30 dakikalık periyotlar halinde verilebilir.");
        }

        if (randevuRepository.existsByDoktorIdAndTarihSaatAndDurumNotAndIdNot(
                request.getDoktorId(), request.getTarihSaat(), RandevuDurumu.IPTAL, id)) {
            throw new ConflictException(
                "RANDEVU ÇAKIŞMASI: " + doktor.getFullName() +
                " için " + request.getTarihSaat() + " saatinde başka randevu bulunmaktadır."
            );
        }

        mevcutRandevu.setHasta(hasta);
        mevcutRandevu.setDoktor(doktor);
        mevcutRandevu.setTarihSaat(request.getTarihSaat());
        mevcutRandevu.setNotlar(request.getNotlar());

        Randevu guncellenenRandevu = randevuRepository.save(mevcutRandevu);
        auditLogService.kaydet("RANDEVU_GUNCELLEME", "sistem", id,
            "Randevu güncellendi: Hasta: " + hasta.getFullName() + ", Doktor: " + doktor.getFullName() + ", Tarih: " + request.getTarihSaat());
        return guncellenenRandevu;
    }

    /**
     * Randevu durumunu günceller.
     */
    public Randevu durumGuncelle(Long randevuId, RandevuDurumu yeniDurum) {
        Randevu randevu = randevuGetir(randevuId);
        randevu.setDurum(yeniDurum);
        return randevuRepository.save(randevu);
    }

    /**
     * Randevu iptal eder.
     */
    public Randevu randevuIptal(Long randevuId) {
        log.info("Randevu iptal ediliyor: ID {}", randevuId);
        Randevu iptalRandevu = durumGuncelle(randevuId, RandevuDurumu.IPTAL);
        auditLogService.kaydet("RANDEVU_IPTAL", "sistem", randevuId, "Randevu ID " + randevuId + " iptal edildi.");
        return iptalRandevu;
    }

    /**
     * ID ile randevu getirir.
     */
    @Transactional(readOnly = true)
    public Randevu randevuGetir(Long id) {
        return randevuRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Randevu bulunamadı: ID " + id));
    }

    /**
     * Tüm randevuları listeler.
     */
    @Transactional(readOnly = true)
    public List<Randevu> tumRandevulariGetir() {
        return randevuRepository.findAll();
    }

    /**
     * Hastanın randevularını listeler.
     */
    @Transactional(readOnly = true)
    public List<Randevu> hastaRandevulari(Long hastaId) {
        return randevuRepository.findByHastaId(hastaId);
    }

    /**
     * Doktorun randevularını listeler.
     */
    @Transactional(readOnly = true)
    public List<Randevu> doktorRandevulari(Long doktorId) {
        return randevuRepository.findByDoktorId(doktorId);
    }

    /**
     * Doktorun bekleyen randevularını listeler.
     */
    @Transactional(readOnly = true)
    public List<Randevu> doktorBekleyenRandevular(Long doktorId) {
        return randevuRepository.findByDoktorIdAndDurum(doktorId, RandevuDurumu.BEKLIYOR);
    }

    /**
     * Doktorun belirtilen tarihteki müsait saatlerini döner.
     */
    @Transactional(readOnly = true)
    public List<String> getMusaitSaatler(Long doktorId, LocalDate tarih) {
        List<String> tumSaatler = new ArrayList<>();
        // 09:00 - 16:30 arası 30'ar dakikalık periyotlar. 12:00-13:00 hariç.
        for (int h = calismaBaslangicSaati; h < calismaBitisSaati; h++) {
            if (h == ogleMolasiSaati) continue; // Öğle molası
            tumSaatler.add(String.format("%02d:00", h));
            tumSaatler.add(String.format("%02d:30", h));
        }

        // O günkü doktorun dolu randevularını getir
        LocalDateTime baslangic = tarih.atStartOfDay();
        LocalDateTime bitis = tarih.atTime(23, 59, 59);
        List<Randevu> doluRandevular = randevuRepository.findCakisanRandevular(doktorId, baslangic, bitis, RandevuDurumu.IPTAL);

        // Dolu saatleri listeden çıkar
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        for (Randevu r : doluRandevular) {
            String doluSaat = r.getTarihSaat().format(formatter);
            tumSaatler.remove(doluSaat);
        }

        return tumSaatler;
    }

    @Transactional(readOnly = true)
    public List<AlternatifTarihResponse> getAlternatifTarihler(Long klinikId, LocalDate baslangicTarihi, int gunSayisi) {
        List<Doktor> doktorlar = doktorRepository.findByKlinikId(klinikId);
        List<AlternatifTarihResponse> oneriler = new ArrayList<>();
        LocalDate tarih = baslangicTarihi.plusDays(1);
        LocalDate bitisTarihi = baslangicTarihi.plusDays(gunSayisi);
        while (tarih.isBefore(bitisTarihi) && oneriler.size() < 5) {
            for (Doktor doktor : doktorlar) {
                List<String> musaitSaatler = getMusaitSaatler(doktor.getId(), tarih);
                if (!musaitSaatler.isEmpty()) {
                    String ilkSaat = musaitSaatler.get(0);
                    String[] parts = ilkSaat.split(":");
                    LocalDateTime oneri = tarih.atTime(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                    oneriler.add(new AlternatifTarihResponse(doktor.getId(), doktor.getFullName(), oneri));
                    if (oneriler.size() >= 5) break;
                }
            }
            tarih = tarih.plusDays(1);
        }
        return oneriler;
    }

    @Transactional(readOnly = true)
    public List<KlinikMusaitlikResponse> getKlinikMusaitSaatler(Long klinikId, LocalDate tarih) {
        List<Doktor> doktorlar = doktorRepository.findByKlinikId(klinikId);
        List<KlinikMusaitlikResponse> sonuc = new ArrayList<>();
        for (Doktor doktor : doktorlar) {
            List<String> saatler = getMusaitSaatler(doktor.getId(), tarih);
            if (!saatler.isEmpty()) {
                sonuc.add(new KlinikMusaitlikResponse(doktor.getId(), doktor.getFullName(), saatler));
            }
        }
        return sonuc;
    }
}
