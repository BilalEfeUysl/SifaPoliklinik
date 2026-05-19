package com.sifa.poliklinik.service;

import com.sifa.poliklinik.dto.HastaRequest;
import com.sifa.poliklinik.exception.ConflictException;
import com.sifa.poliklinik.exception.NotFoundException;
import com.sifa.poliklinik.model.Hasta;
import com.sifa.poliklinik.repository.HastaRepository;
import com.sifa.poliklinik.repository.OdemeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * Hasta yönetimi servisi.
 * Hasta CRUD işlemleri ve borç kontrolü.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class HastaService {

    private final HastaRepository hastaRepository;
    private final OdemeRepository odemeRepository;
    private final AuditLogService auditLogService;

    /**
     * Yeni hasta kaydı oluşturur.
     */
    public Hasta hastaKaydet(HastaRequest request) {
        if (hastaRepository.existsByTcKimlik(request.getTcKimlik())) {
            throw new ConflictException("Bu TC Kimlik numarası ile kayıtlı hasta zaten mevcut: " + request.getTcKimlik());
        }

        Hasta hasta = Hasta.builder()
                .tcKimlik(request.getTcKimlik())
                .ad(request.getAd())
                .soyad(request.getSoyad())
                .dogumTarihi(request.getDogumTarihi())
                .telefon(request.getTelefon())
                .adres(request.getAdres())
                .sgkDurumu(request.getSgkDurumu())
                .build();

        Hasta kaydedilenHasta = hastaRepository.save(hasta);
        log.info("Hasta kaydedildi: {} {} (TC: {})", kaydedilenHasta.getAd(), kaydedilenHasta.getSoyad(), kaydedilenHasta.getTcKimlik());
        return kaydedilenHasta;
    }

    /**
     * Hasta bilgilerini günceller.
     */
    public Hasta hastaGuncelle(Long id, HastaRequest request) {
        Hasta hasta = hastaGetir(id);
        hasta.setAd(request.getAd());
        hasta.setSoyad(request.getSoyad());
        hasta.setDogumTarihi(request.getDogumTarihi());
        hasta.setTelefon(request.getTelefon());
        hasta.setAdres(request.getAdres());
        hasta.setSgkDurumu(request.getSgkDurumu());
        Hasta guncellenenHasta = hastaRepository.save(hasta);
        log.info("Hasta güncellendi: ID {}", id);
        return guncellenenHasta;
    }

    /**
     * ID ile hasta getirir.
     */
    @Transactional(readOnly = true)
    public Hasta hastaGetir(Long id) {
        return hastaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Hasta bulunamadı: ID " + id));
    }

    /**
     * TC Kimlik ile hasta getirir.
     */
    @Transactional(readOnly = true)
    public Hasta hastaTcIleGetir(String tcKimlik) {
        return hastaRepository.findByTcKimlik(tcKimlik)
                .orElseThrow(() -> new NotFoundException("Hasta bulunamadı: TC " + tcKimlik));
    }

    /**
     * Tüm hastaları listeler.
     */
    @Transactional(readOnly = true)
    public List<Hasta> tumHastalariGetir() {
        return hastaRepository.findAll();
    }

    /**
     * Tüm hastaları sayfalama ile listeler.
     */
    @Transactional(readOnly = true)
    public Page<Hasta> tumHastalariGetirSayfalama(int sayfa, int boyut) {
        Pageable pageable = PageRequest.of(sayfa, boyut, Sort.by(Sort.Direction.DESC, "kayitTarihi"));
        return hastaRepository.findAll(pageable);
    }

    /**
     * Ad, soyad veya TC ile arama yapar.
     */
    @Transactional(readOnly = true)
    public List<Hasta> hastaAra(String arama) {
        return hastaRepository.aramaYap(arama);
    }

    /**
     * Hastayı siler.
     */
    public void hastaSil(Long id) {
        if (!hastaRepository.existsById(id)) {
            throw new NotFoundException("Silinecek hasta bulunamadı: ID " + id);
        }
        log.info("Hasta siliniyor: ID {}", id);
        hastaRepository.deleteById(id);
        auditLogService.kaydet("HASTA_SILME", "sistem", id, "Hasta ID " + id + " silindi.");
    }

    /**
     * Hastanın ödenmemiş borç tutarını döner.
     */
    @Transactional(readOnly = true)
    public Double borcKontrol(Long hastaId) {
        return odemeRepository.getToplamBorc(hastaId);
    }

    /**
     * Hastanın borçlu olup olmadığını kontrol eder.
     * İş Kuralı: Borçlu hastaya randevu verilemez!
     */
    @Transactional(readOnly = true)
    public boolean borcluMu(Long hastaId) {
        Double borc = odemeRepository.getToplamBorc(hastaId);
        return borc != null && borc > 0;
    }
}
