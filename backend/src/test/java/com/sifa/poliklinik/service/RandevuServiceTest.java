package com.sifa.poliklinik.service;

import com.sifa.poliklinik.dto.RandevuRequest;
import com.sifa.poliklinik.exception.BusinessRuleException;
import com.sifa.poliklinik.exception.ConflictException;
import com.sifa.poliklinik.exception.NotFoundException;
import com.sifa.poliklinik.model.Doktor;
import com.sifa.poliklinik.model.Hasta;
import com.sifa.poliklinik.model.Kullanici;
import com.sifa.poliklinik.model.Randevu;
import com.sifa.poliklinik.model.Klinik;
import com.sifa.poliklinik.model.enums.RandevuDurumu;
import com.sifa.poliklinik.repository.DoktorRepository;
import com.sifa.poliklinik.repository.HastaRepository;
import com.sifa.poliklinik.repository.RandevuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RandevuService Unit Testleri")
class RandevuServiceTest {

    @Mock
    private RandevuRepository randevuRepository;

    @Mock
    private HastaRepository hastaRepository;

    @Mock
    private DoktorRepository doktorRepository;

    @Mock
    private HastaService hastaService;

    @InjectMocks
    private RandevuService randevuService;

    // Sabit test veri nesneleri
    private Hasta testHasta;
    private Doktor testDoktor;
    private RandevuRequest gecerliRequest;

    @BeforeEach
    void setUp() {
        // @Value alanlarını ReflectionTestUtils ile enjekte et
        ReflectionTestUtils.setField(randevuService, "calismaBaslangicSaati", 9);
        ReflectionTestUtils.setField(randevuService, "calismaBitisSaati", 17);
        ReflectionTestUtils.setField(randevuService, "ogleMolasiSaati", 12);

        // Kullanici nesnesi (Doktor.getFullName() için gerekli)
        Kullanici kullanici = Kullanici.builder()
                .id(1L)
                .ad("Ahmet")
                .soyad("Yilmaz")
                .email("ahmet.yilmaz@sifa.com")
                .sifre("sifre123")
                .build();

        Klinik klinik = Klinik.builder()
                .id(1L)
                .ad("Kardiyoloji")
                .muayeneUcreti(300.0)
                .build();

        testHasta = Hasta.builder()
                .id(1L)
                .tcKimlik("12345678901")
                .ad("Fatma")
                .soyad("Kaya")
                .build();

        testDoktor = Doktor.builder()
                .id(1L)
                .unvan("Dr.")
                .uzmanlikAlani("Kardiyoloji")
                .kullanici(kullanici)
                .klinik(klinik)
                .build();

        // Geçerli (çakışmasız, mesai içi, doğru dakika) bir randevu isteği
        gecerliRequest = new RandevuRequest();
        gecerliRequest.setHastaId(1L);
        gecerliRequest.setDoktorId(1L);
        gecerliRequest.setTarihSaat(LocalDateTime.of(2025, 6, 10, 10, 0));
        gecerliRequest.setNotlar("Test notu");
    }

    // ─────────────────────────────────────────────
    // 1. Hasta bulunamadı → NotFoundException
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("randevuOlustur: hasta yok → NotFoundException fırlatır")
    void randevuOlustur_hastaYok_throwsNotFoundException() {
        when(hastaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> randevuService.randevuOlustur(gecerliRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("1");

        verify(hastaRepository).findById(1L);
        verifyNoInteractions(doktorRepository, randevuRepository);
    }

    // ─────────────────────────────────────────────
    // 2. Hasta borçlu → BusinessRuleException
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("randevuOlustur: hasta borçlu → BusinessRuleException fırlatır")
    void randevuOlustur_hastaBorclu_throwsBusinessRuleException() {
        when(hastaRepository.findById(1L)).thenReturn(Optional.of(testHasta));
        when(hastaService.borcluMu(1L)).thenReturn(true);

        assertThatThrownBy(() -> randevuService.randevuOlustur(gecerliRequest))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("RANDEVU REDDEDİLDİ");

        verify(hastaService).borcluMu(1L);
        verifyNoInteractions(doktorRepository, randevuRepository);
    }

    // ─────────────────────────────────────────────
    // 3. Doktor bulunamadı → NotFoundException
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("randevuOlustur: doktor yok → NotFoundException fırlatır")
    void randevuOlustur_doktorYok_throwsNotFoundException() {
        when(hastaRepository.findById(1L)).thenReturn(Optional.of(testHasta));
        when(hastaService.borcluMu(1L)).thenReturn(false);
        when(doktorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> randevuService.randevuOlustur(gecerliRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("1");

        verify(doktorRepository).findById(1L);
        verifyNoInteractions(randevuRepository);
    }

    // ─────────────────────────────────────────────
    // 4. Mesai dışı saat (08:00) → BusinessRuleException
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("randevuOlustur: mesai dışı saat (08:00) → BusinessRuleException fırlatır")
    void randevuOlustur_mesaiDisiSaat_throwsBusinessRuleException() {
        gecerliRequest.setTarihSaat(LocalDateTime.of(2025, 6, 10, 8, 0)); // Saat 08 → mesai öncesi

        when(hastaRepository.findById(1L)).thenReturn(Optional.of(testHasta));
        when(hastaService.borcluMu(1L)).thenReturn(false);
        when(doktorRepository.findById(1L)).thenReturn(Optional.of(testDoktor));

        assertThatThrownBy(() -> randevuService.randevuOlustur(gecerliRequest))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("09:00");

        verifyNoInteractions(randevuRepository);
    }

    // ─────────────────────────────────────────────
    // 5. Öğle molası saati (12:00) → BusinessRuleException
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("randevuOlustur: öğle molası saati (12:00) → BusinessRuleException fırlatır")
    void randevuOlustur_ogleMolasi_throwsBusinessRuleException() {
        gecerliRequest.setTarihSaat(LocalDateTime.of(2025, 6, 10, 12, 0)); // Öğle molası

        when(hastaRepository.findById(1L)).thenReturn(Optional.of(testHasta));
        when(hastaService.borcluMu(1L)).thenReturn(false);
        when(doktorRepository.findById(1L)).thenReturn(Optional.of(testDoktor));

        assertThatThrownBy(() -> randevuService.randevuOlustur(gecerliRequest))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("öğle molası");

        verifyNoInteractions(randevuRepository);
    }

    // ─────────────────────────────────────────────
    // 6. Yanlış dakika (15 geçe) → BusinessRuleException
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("randevuOlustur: yanlış dakika (15 geçe) → BusinessRuleException fırlatır")
    void randevuOlustur_yanlisDakika_throwsBusinessRuleException() {
        gecerliRequest.setTarihSaat(LocalDateTime.of(2025, 6, 10, 10, 15)); // 15 dakika → geçersiz

        when(hastaRepository.findById(1L)).thenReturn(Optional.of(testHasta));
        when(hastaService.borcluMu(1L)).thenReturn(false);
        when(doktorRepository.findById(1L)).thenReturn(Optional.of(testDoktor));

        assertThatThrownBy(() -> randevuService.randevuOlustur(gecerliRequest))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("30 dakikalık");

        verifyNoInteractions(randevuRepository);
    }

    // ─────────────────────────────────────────────
    // 7. Çakışan randevu → ConflictException
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("randevuOlustur: çakışan randevu → ConflictException fırlatır")
    void randevuOlustur_cakisanRandevu_throwsConflictException() {
        LocalDateTime tarihSaat = LocalDateTime.of(2025, 6, 10, 10, 0);
        gecerliRequest.setTarihSaat(tarihSaat);

        when(hastaRepository.findById(1L)).thenReturn(Optional.of(testHasta));
        when(hastaService.borcluMu(1L)).thenReturn(false);
        when(doktorRepository.findById(1L)).thenReturn(Optional.of(testDoktor));
        when(randevuRepository.existsByDoktorIdAndTarihSaatAndDurumNot(
                eq(1L), eq(tarihSaat), eq(RandevuDurumu.IPTAL)))
                .thenReturn(true);

        assertThatThrownBy(() -> randevuService.randevuOlustur(gecerliRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("RANDEVU ÇAKIŞMASI");

        verify(randevuRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────
    // 8. Başarılı senaryo → randevu kaydedilir
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("randevuOlustur: başarılı senaryo → randevu kaydedilip döndürülür")
    void randevuOlustur_basarili_randevuKaydedilir() {
        LocalDateTime tarihSaat = LocalDateTime.of(2025, 6, 10, 10, 0);
        gecerliRequest.setTarihSaat(tarihSaat);

        Randevu beklenenRandevu = Randevu.builder()
                .id(99L)
                .hasta(testHasta)
                .doktor(testDoktor)
                .tarihSaat(tarihSaat)
                .durum(RandevuDurumu.BEKLIYOR)
                .notlar("Test notu")
                .build();

        when(hastaRepository.findById(1L)).thenReturn(Optional.of(testHasta));
        when(hastaService.borcluMu(1L)).thenReturn(false);
        when(doktorRepository.findById(1L)).thenReturn(Optional.of(testDoktor));
        when(randevuRepository.existsByDoktorIdAndTarihSaatAndDurumNot(
                eq(1L), eq(tarihSaat), eq(RandevuDurumu.IPTAL)))
                .thenReturn(false);
        when(randevuRepository.save(any(Randevu.class))).thenReturn(beklenenRandevu);

        Randevu sonuc = randevuService.randevuOlustur(gecerliRequest);

        assertThat(sonuc).isNotNull();
        assertThat(sonuc.getId()).isEqualTo(99L);
        assertThat(sonuc.getDurum()).isEqualTo(RandevuDurumu.BEKLIYOR);
        assertThat(sonuc.getHasta()).isEqualTo(testHasta);
        assertThat(sonuc.getDoktor()).isEqualTo(testDoktor);

        verify(randevuRepository).save(any(Randevu.class));
    }
}
