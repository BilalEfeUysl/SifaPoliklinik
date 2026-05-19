package com.sifa.poliklinik.service;

import com.sifa.poliklinik.dto.MuayeneRequest;
import com.sifa.poliklinik.exception.BusinessRuleException;
import com.sifa.poliklinik.exception.ConflictException;
import com.sifa.poliklinik.exception.NotFoundException;
import com.sifa.poliklinik.model.MuayeneKaydi;
import com.sifa.poliklinik.model.Odeme;
import com.sifa.poliklinik.model.Randevu;
import com.sifa.poliklinik.model.enums.OdemeDurumu;
import com.sifa.poliklinik.model.enums.RandevuDurumu;
import com.sifa.poliklinik.repository.MuayeneRepository;
import com.sifa.poliklinik.repository.OdemeRepository;
import com.sifa.poliklinik.repository.RandevuRepository;
import com.sifa.poliklinik.service.sgk.SGKAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Muayene kaydı servisi.
 * Doktorun muayene sonuçlarını (tanı, reçete, rapor) kaydetmesi.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MuayeneService {

    private final MuayeneRepository muayeneRepository;
    private final RandevuRepository randevuRepository;
    private final OdemeRepository odemeRepository;
    private final SGKAdapter sgkAdapter;

    @Value("${poliklinik.muayene.varsayilan-ucret:200.0}")
    private double varsayilanMuayeneUcreti;

    /**
     * Muayene kaydı oluşturur ve randevu durumunu TAMAMLANDI yapar.
     */
    public MuayeneKaydi muayeneKaydet(MuayeneRequest request) {
        Randevu randevu = randevuRepository.findById(request.getRandevuId())
                .orElseThrow(() -> new NotFoundException("Randevu bulunamadı: ID " + request.getRandevuId()));

        if (randevu.getDurum() == RandevuDurumu.IPTAL) {
            throw new BusinessRuleException("İptal edilmiş randevu için muayene kaydı oluşturulamaz.");
        }

        if (randevu.getDurum() == RandevuDurumu.TAMAMLANDI) {
            throw new ConflictException("Bu randevu için zaten muayene kaydı mevcut.");
        }

        MuayeneKaydi muayene = MuayeneKaydi.builder()
                .randevu(randevu)
                .tani(request.getTani())
                .recete(request.getRecete())
                .rapor(request.getRapor())
                .sevkKurumu(request.getSevkKurumu())
                .build();

        // Randevu durumunu güncelle
        randevu.setDurum(RandevuDurumu.TAMAMLANDI);
        randevuRepository.save(randevu);

        MuayeneKaydi kaydedilenMuayene = muayeneRepository.save(muayene);
        log.info("Muayene kaydedildi: RandevuID={}, MuayeneID={}", request.getRandevuId(), kaydedilenMuayene.getId());

        // Prevent duplicate payment
        if (odemeRepository.findByMuayeneKaydiId(kaydedilenMuayene.getId()).isPresent()) {
            log.warn("Muayene için ödeme zaten mevcut, yeni ödeme oluşturulmadı: MuayeneID={}", kaydedilenMuayene.getId());
            return kaydedilenMuayene; // Already has a payment, skip
        }

        // Arka planda otomatik Ödeme kaydı oluştur
        Double toplamTutar = randevu.getDoktor().getKlinik().getMuayeneUcreti();
        if (toplamTutar == null) toplamTutar = varsayilanMuayeneUcreti;
        double indirimOrani = sgkAdapter.sgkIndirimOrani(randevu.getHasta().getTcKimlik());
        double sgkIndirimi = toplamTutar * indirimOrani;
        double netTutar = toplamTutar - sgkIndirimi;

        Odeme odeme = Odeme.builder()
                .hasta(randevu.getHasta())
                .muayeneKaydi(kaydedilenMuayene)
                .toplamTutar(toplamTutar)
                .sgkIndirimi(sgkIndirimi)
                .netTutar(netTutar)
                .odemeDurumu(OdemeDurumu.BEKLIYOR)
                .build();
        odemeRepository.save(odeme);

        return kaydedilenMuayene;
    }

    /**
     * Muayene kaydını günceller.
     */
    public MuayeneKaydi muayeneGuncelle(Long id, MuayeneRequest request) {
        MuayeneKaydi muayene = muayeneGetir(id);
        muayene.setTani(request.getTani());
        muayene.setRecete(request.getRecete());
        muayene.setRapor(request.getRapor());
        muayene.setSevkKurumu(request.getSevkKurumu());
        return muayeneRepository.save(muayene);
    }

    /**
     * ID ile muayene kaydı getirir.
     */
    @Transactional(readOnly = true)
    public MuayeneKaydi muayeneGetir(Long id) {
        return muayeneRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Muayene kaydı bulunamadı: ID " + id));
    }

    /**
     * Randevu ID ile muayene kaydı getirir.
     */
    @Transactional(readOnly = true)
    public MuayeneKaydi randevuIleMuayeneGetir(Long randevuId) {
        return muayeneRepository.findByRandevuId(randevuId)
                .orElseThrow(() -> new NotFoundException("Bu randevu için muayene kaydı bulunamadı."));
    }

    /**
     * Hastanın tüm muayene kayıtlarını listeler.
     */
    @Transactional(readOnly = true)
    public List<MuayeneKaydi> hastaMuayeneleri(Long hastaId) {
        return muayeneRepository.findByRandevuHastaId(hastaId);
    }

    /**
     * Doktorun tüm muayene kayıtlarını listeler.
     */
    @Transactional(readOnly = true)
    public List<MuayeneKaydi> doktorMuayeneleri(Long doktorId) {
        return muayeneRepository.findByRandevuDoktorId(doktorId);
    }

    @Transactional(readOnly = true)
    public List<MuayeneKaydi> tumMuayeneler() {
        return muayeneRepository.findAll();
    }
}
