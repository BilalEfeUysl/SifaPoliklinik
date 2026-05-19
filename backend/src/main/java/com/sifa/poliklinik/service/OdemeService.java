package com.sifa.poliklinik.service;

import com.sifa.poliklinik.exception.ConflictException;
import com.sifa.poliklinik.exception.NotFoundException;
import com.sifa.poliklinik.model.*;
import com.sifa.poliklinik.model.enums.OdemeDurumu;
import com.sifa.poliklinik.model.enums.OdemeTipi;
import com.sifa.poliklinik.repository.OdemeRepository;
import com.sifa.poliklinik.service.sgk.SGKAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Ödeme yönetimi servisi.
 * SGK Adapter kullanarak indirim hesaplaması yapar.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OdemeService {

    private final OdemeRepository odemeRepository;
    private final SGKAdapter sgkAdapter;
    private final MuayeneService muayeneService;
    private final AuditLogService auditLogService;

    @Value("${poliklinik.muayene.varsayilan-ucret:200.0}")
    private double varsayilanMuayeneUcreti;

    /**
     * Muayene sonrası ödeme kaydı oluşturur.
     * SGK Adapter ile indirim hesaplaması yapılır.
     */
    public Odeme odemeOlustur(Long muayeneId) {
        // Check if payment already exists for this examination
        if (odemeRepository.findByMuayeneKaydiId(muayeneId).isPresent()) {
            log.warn("Mükerrer ödeme denemesi: MuayeneID={}", muayeneId);
            throw new ConflictException("Bu muayene için zaten ödeme kaydı mevcut.");
        }

        MuayeneKaydi muayene = muayeneService.muayeneGetir(muayeneId);
        Hasta hasta = muayene.getRandevu().getHasta();
        Klinik klinik = muayene.getRandevu().getDoktor().getKlinik();

        Double toplamTutar = klinik.getMuayeneUcreti();
        if (toplamTutar == null) {
            toplamTutar = varsayilanMuayeneUcreti; // Varsayılan muayene ücreti
        }

        // SGK Adapter Pattern ile indirim hesaplama
        double indirimOrani = sgkAdapter.sgkIndirimOrani(hasta.getTcKimlik());
        double sgkIndirimi = toplamTutar * indirimOrani;
        double netTutar = toplamTutar - sgkIndirimi;

        log.info("Ödeme oluşturuluyor: Hasta={}, Toplam={}, SGK İndirim={} ({}%), Net={}",
                hasta.getFullName(), toplamTutar, sgkIndirimi, indirimOrani * 100, netTutar);

        Odeme odeme = Odeme.builder()
                .hasta(hasta)
                .muayeneKaydi(muayene)
                .toplamTutar(toplamTutar)
                .sgkIndirimi(sgkIndirimi)
                .netTutar(netTutar)
                .sgkAktif(indirimOrani > 0)
                .odemeDurumu(OdemeDurumu.BEKLIYOR)
                .build();

        return odemeRepository.save(odeme);
    }

    public Odeme odemeTahsilat(Long odemeId, OdemeTipi odemeTipi) {
        Odeme odeme = odemeGetir(odemeId);
        if (odeme.getOdemeDurumu() == OdemeDurumu.ODENDI) {
            throw new ConflictException("Bu ödeme zaten tahsil edilmiş.");
        }
        if (odemeTipi == null) {
            throw new com.sifa.poliklinik.exception.BusinessRuleException("Ödeme tipi seçilmelidir.");
        }
        odeme.setOdemeDurumu(OdemeDurumu.ODENDI);
        odeme.setOdemeTipi(odemeTipi);
        odeme.setOdemeTarihi(LocalDateTime.now());
        log.info("Ödeme tahsil edildi: ID={}, Tutar={} TL", odemeId, odeme.getNetTutar());
        Odeme kaydedilenOdeme = odemeRepository.save(odeme);
        auditLogService.kaydet("ODEME_TAHSILAT", "sistem", odemeId,
            "Ödeme ID " + odemeId + " tahsil edildi. Tutar: " + odeme.getNetTutar() + " TL, Tip: " + odemeTipi);
        return kaydedilenOdeme;
    }

    public Odeme sgkYenidenSorgula(Long odemeId) {
        Odeme odeme = odemeGetir(odemeId);
        if (odeme.getOdemeDurumu() == OdemeDurumu.ODENDI) {
            throw new com.sifa.poliklinik.exception.BusinessRuleException("Ödenmiş ödeme için SGK sorgulanamaz.");
        }
        Hasta hasta = odeme.getHasta();
        try {
            double indirimOrani = sgkAdapter.sgkIndirimOrani(hasta.getTcKimlik());
            double sgkIndirimi = odeme.getToplamTutar() * indirimOrani;
            double netTutar = odeme.getToplamTutar() - sgkIndirimi;
            odeme.setSgkIndirimi(sgkIndirimi);
            odeme.setNetTutar(netTutar);
            odeme.setSgkAktif(indirimOrani > 0);
            log.info("SGK yeniden sorgulandı: Hasta={}, İndirim={}, Net={}", hasta.getFullName(), sgkIndirimi, netTutar);
            return odemeRepository.save(odeme);
        } catch (Exception e) {
            log.error("SGK sorgulama hatası: OdemeID={}, Hasta={}, Hata={}", odemeId, hasta.getTcKimlik(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Ödemeyi iptal eder.
     */
    public Odeme odemeIptal(Long odemeId) {
        Odeme odeme = odemeGetir(odemeId);
        odeme.setOdemeDurumu(OdemeDurumu.IPTAL);
        return odemeRepository.save(odeme);
    }

    /**
     * ID ile ödeme getirir.
     */
    @Transactional(readOnly = true)
    public Odeme odemeGetir(Long id) {
        return odemeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ödeme bulunamadı: ID " + id));
    }

    /**
     * Tüm ödemeleri listeler.
     */
    @Transactional(readOnly = true)
    public List<Odeme> tumOdemeleriGetir() {
        return odemeRepository.findAll();
    }

    /**
     * Hastanın ödemelerini listeler.
     */
    @Transactional(readOnly = true)
    public List<Odeme> hastaOdemeleri(Long hastaId) {
        return odemeRepository.findByHastaId(hastaId);
    }

    /**
     * Bekleyen ödemeleri listeler.
     */
    @Transactional(readOnly = true)
    public List<Odeme> bekleyenOdemeler() {
        return odemeRepository.findByOdemeDurumu(OdemeDurumu.BEKLIYOR);
    }

    /**
     * Hastanın toplam borcunu döner.
     */
    @Transactional(readOnly = true)
    public Double hastaToplamBorc(Long hastaId) {
        return odemeRepository.getToplamBorc(hastaId);
    }
}
