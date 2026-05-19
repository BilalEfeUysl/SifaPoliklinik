package com.sifa.poliklinik.service;

import com.sifa.poliklinik.dto.HastaRequest;
import com.sifa.poliklinik.exception.ConflictException;
import com.sifa.poliklinik.exception.NotFoundException;
import com.sifa.poliklinik.model.Hasta;
import com.sifa.poliklinik.model.enums.SGKDurumu;
import com.sifa.poliklinik.repository.HastaRepository;
import com.sifa.poliklinik.repository.OdemeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HastaService Unit Testleri")
class HastaServiceTest {

    @Mock
    private HastaRepository hastaRepository;

    @Mock
    private OdemeRepository odemeRepository;

    @InjectMocks
    private HastaService hastaService;

    private HastaRequest testRequest;
    private Hasta testHasta;

    @BeforeEach
    void setUp() {
        testRequest = new HastaRequest();
        testRequest.setTcKimlik("12345678901");
        testRequest.setAd("Fatma");
        testRequest.setSoyad("Kaya");
        testRequest.setDogumTarihi(LocalDate.of(1990, 5, 20));
        testRequest.setTelefon("05001234567");
        testRequest.setAdres("Ankara");
        testRequest.setSgkDurumu(SGKDurumu.AKTIF);

        testHasta = Hasta.builder()
                .id(1L)
                .tcKimlik("12345678901")
                .ad("Fatma")
                .soyad("Kaya")
                .dogumTarihi(LocalDate.of(1990, 5, 20))
                .telefon("05001234567")
                .adres("Ankara")
                .sgkDurumu(SGKDurumu.AKTIF)
                .build();
    }

    // ─────────────────────────────────────────────
    // 1. TC mevcut → ConflictException
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("hastaKaydet: TC kimlik zaten mevcut → ConflictException fırlatır")
    void hastaKaydet_tcMevcut_throwsConflictException() {
        when(hastaRepository.existsByTcKimlik("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> hastaService.hastaKaydet(testRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("12345678901");

        verify(hastaRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────
    // 2. Yeni hasta → başarıyla kaydedilir
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("hastaKaydet: yeni hasta → kaydedilip döndürülür")
    void hastaKaydet_yeniHasta_kaydedilir() {
        when(hastaRepository.existsByTcKimlik("12345678901")).thenReturn(false);
        when(hastaRepository.save(any(Hasta.class))).thenReturn(testHasta);

        Hasta sonuc = hastaService.hastaKaydet(testRequest);

        assertThat(sonuc).isNotNull();
        assertThat(sonuc.getTcKimlik()).isEqualTo("12345678901");
        assertThat(sonuc.getAd()).isEqualTo("Fatma");
        assertThat(sonuc.getSoyad()).isEqualTo("Kaya");
        assertThat(sonuc.getSgkDurumu()).isEqualTo(SGKDurumu.AKTIF);

        verify(hastaRepository).save(any(Hasta.class));
    }

    // ─────────────────────────────────────────────
    // 3. Hasta yok → NotFoundException
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("hastaGetir: hasta bulunamadı → NotFoundException fırlatır")
    void hastaGetir_hastaYok_throwsNotFoundException() {
        when(hastaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hastaService.hastaGetir(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // ─────────────────────────────────────────────
    // 4. Hasta yok → silme sırasında NotFoundException
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("hastaSil: hasta bulunamadı → NotFoundException fırlatır")
    void hastaSil_hastaYok_throwsNotFoundException() {
        when(hastaRepository.existsById(42L)).thenReturn(false);

        assertThatThrownBy(() -> hastaService.hastaSil(42L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("42");

        verify(hastaRepository, never()).deleteById(any());
    }

    // ─────────────────────────────────────────────
    // 5. Borcu var → borcluMu() true döner
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("borcluMu: ödenmemiş borç var → true döner")
    void borcluMu_borcuVar_returnsTrue() {
        when(odemeRepository.getToplamBorc(1L)).thenReturn(150.0);

        boolean sonuc = hastaService.borcluMu(1L);

        assertThat(sonuc).isTrue();
        verify(odemeRepository).getToplamBorc(1L);
    }

    // ─────────────────────────────────────────────
    // 6. Borcu yok (0.0) → borcluMu() false döner
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("borcluMu: ödenmemiş borç yok (0.0) → false döner")
    void borcluMu_borcuYok_returnsFalse_sifir() {
        when(odemeRepository.getToplamBorc(1L)).thenReturn(0.0);

        boolean sonuc = hastaService.borcluMu(1L);

        assertThat(sonuc).isFalse();
    }

    // ─────────────────────────────────────────────
    // 7. Borcu yok (null) → borcluMu() false döner
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("borcluMu: ödenmemiş borç yok (null) → false döner")
    void borcluMu_borcuYok_returnsFalse_null() {
        when(odemeRepository.getToplamBorc(1L)).thenReturn(null);

        boolean sonuc = hastaService.borcluMu(1L);

        assertThat(sonuc).isFalse();
    }
}
