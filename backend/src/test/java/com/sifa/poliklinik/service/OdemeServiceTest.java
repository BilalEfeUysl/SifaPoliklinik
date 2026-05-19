package com.sifa.poliklinik.service;

import com.sifa.poliklinik.exception.ConflictException;
import com.sifa.poliklinik.model.*;
import com.sifa.poliklinik.model.enums.OdemeDurumu;
import com.sifa.poliklinik.model.enums.OdemeTipi;
import com.sifa.poliklinik.model.enums.SGKDurumu;
import com.sifa.poliklinik.repository.OdemeRepository;
import com.sifa.poliklinik.service.sgk.SGKAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OdemeService Unit Testleri")
class OdemeServiceTest {

    @Mock
    private OdemeRepository odemeRepository;

    @Mock
    private SGKAdapter sgkAdapter;

    @Mock
    private MuayeneService muayeneService;

    @InjectMocks
    private OdemeService odemeService;

    // Varsayılan muayene ücreti (uygulama.yml: poliklinik.muayene.varsayilan-ucret)
    private static final double VARSAYILAN_UCRET = 200.0;

    // SGK aktif indirim oranı: %80
    private static final double SGK_AKTIF_INDIRIM = 0.80;

    private Hasta hastaAktif;   // SGK_DURUMU = AKTIF
    private Hasta hastaPasif;   // SGK_DURUMU = PASIF
    private Klinik klinik;
    private MuayeneKaydi muayeneAktif;
    private MuayeneKaydi muayenePasif;

    @BeforeEach
    void setUp() {
        // @Value alanını ReflectionTestUtils ile enjekte et
        ReflectionTestUtils.setField(odemeService, "varsayilanMuayeneUcreti", VARSAYILAN_UCRET);

        Kullanici kullanici = Kullanici.builder()
                .id(10L)
                .ad("Mehmet")
                .soyad("Oz")
                .email("mehmet.oz@sifa.com")
                .sifre("sifre456")
                .build();

        klinik = Klinik.builder()
                .id(1L)
                .ad("Göz")
                .muayeneUcreti(300.0)
                .build();

        Doktor doktor = Doktor.builder()
                .id(1L)
                .unvan("Uzm. Dr.")
                .uzmanlikAlani("Göz")
                .kullanici(kullanici)
                .klinik(klinik)
                .build();

        hastaAktif = Hasta.builder()
                .id(1L)
                .tcKimlik("11111111111")
                .ad("Ayse")
                .soyad("Demir")
                .sgkDurumu(SGKDurumu.AKTIF)
                .build();

        hastaPasif = Hasta.builder()
                .id(2L)
                .tcKimlik("22222222222")
                .ad("Ali")
                .soyad("Celik")
                .sgkDurumu(SGKDurumu.PASIF)
                .build();

        Randevu randevuAktif = Randevu.builder()
                .id(100L)
                .hasta(hastaAktif)
                .doktor(doktor)
                .build();

        Randevu randevuPasif = Randevu.builder()
                .id(101L)
                .hasta(hastaPasif)
                .doktor(doktor)
                .build();

        muayeneAktif = MuayeneKaydi.builder()
                .id(10L)
                .randevu(randevuAktif)
                .tani("Test tanı - aktif")
                .build();

        muayenePasif = MuayeneKaydi.builder()
                .id(11L)
                .randevu(randevuPasif)
                .tani("Test tanı - pasif")
                .build();
    }

    // ─────────────────────────────────────────────
    // 1. Mükerrer ödeme → ConflictException
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("odemeOlustur: mükerrer ödeme denemesi → ConflictException fırlatır")
    void odemeOlustur_mukerrerOdeme_throwsConflictException() {
        // Bu muayene ID'si için zaten bir ödeme mevcut
        Odeme mevcutOdeme = Odeme.builder().id(99L).build();
        when(odemeRepository.findByMuayeneKaydiId(10L)).thenReturn(Optional.of(mevcutOdeme));

        assertThatThrownBy(() -> odemeService.odemeOlustur(10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("zaten ödeme kaydı mevcut");

        verify(odemeRepository, never()).save(any());
        verifyNoInteractions(muayeneService, sgkAdapter);
    }

    // ─────────────────────────────────────────────
    // 2. SGK AKTIF → %80 indirim doğru uygulanır
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("odemeOlustur: SGK AKTIF hasta → %80 indirim hesaplanır")
    void odemeOlustur_sgkAktif_dogruIndirimHesabi() {
        double toplamTutar = 300.0; // klinik.muayeneUcreti
        double beklenenIndirim = toplamTutar * SGK_AKTIF_INDIRIM;   // 240.0
        double beklenenNet    = toplamTutar - beklenenIndirim;        // 60.0

        when(odemeRepository.findByMuayeneKaydiId(10L)).thenReturn(Optional.empty());
        when(muayeneService.muayeneGetir(10L)).thenReturn(muayeneAktif);
        when(sgkAdapter.sgkIndirimOrani("11111111111")).thenReturn(SGK_AKTIF_INDIRIM);

        // save çağrısı: Odeme nesnesini olduğu gibi geri döndür
        when(odemeRepository.save(any(Odeme.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Odeme sonuc = odemeService.odemeOlustur(10L);

        assertThat(sonuc).isNotNull();
        assertThat(sonuc.getToplamTutar()).isEqualTo(toplamTutar);
        assertThat(sonuc.getSgkIndirimi()).isCloseTo(beklenenIndirim, within(0.001));
        assertThat(sonuc.getNetTutar()).isCloseTo(beklenenNet, within(0.001));
        assertThat(sonuc.getOdemeDurumu()).isEqualTo(OdemeDurumu.BEKLIYOR);
        assertThat(sonuc.getHasta()).isEqualTo(hastaAktif);

        verify(sgkAdapter).sgkIndirimOrani("11111111111");
        verify(odemeRepository).save(any(Odeme.class));
    }

    // ─────────────────────────────────────────────
    // 3. SGK PASIF → indirim yok, tam ücret
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("odemeOlustur: SGK PASIF hasta → indirim uygulanmaz")
    void odemeOlustur_sgkPasif_indirimYok() {
        double toplamTutar = 300.0; // klinik.muayeneUcreti
        double indirimOrani = 0.0;  // SGK pasif → sıfır indirim

        when(odemeRepository.findByMuayeneKaydiId(11L)).thenReturn(Optional.empty());
        when(muayeneService.muayeneGetir(11L)).thenReturn(muayenePasif);
        when(sgkAdapter.sgkIndirimOrani("22222222222")).thenReturn(indirimOrani);
        when(odemeRepository.save(any(Odeme.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Odeme sonuc = odemeService.odemeOlustur(11L);

        assertThat(sonuc.getToplamTutar()).isEqualTo(toplamTutar);
        assertThat(sonuc.getSgkIndirimi()).isCloseTo(0.0, within(0.001));
        assertThat(sonuc.getNetTutar()).isCloseTo(toplamTutar, within(0.001));
        assertThat(sonuc.getOdemeDurumu()).isEqualTo(OdemeDurumu.BEKLIYOR);
        assertThat(sonuc.getHasta()).isEqualTo(hastaPasif);

        verify(sgkAdapter).sgkIndirimOrani("22222222222");
    }

    // ─────────────────────────────────────────────
    // 4. klinik muayeneUcreti null → varsayılan ücret kullanılır
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("odemeOlustur: klinik ücreti null → varsayılan 200 TL kullanılır")
    void odemeOlustur_klinikUcretiNull_varsayilanKullanilir() {
        // Klinik ücretini null yap
        klinik.setMuayeneUcreti(null);
        double indirimOrani = 0.0;

        when(odemeRepository.findByMuayeneKaydiId(11L)).thenReturn(Optional.empty());
        when(muayeneService.muayeneGetir(11L)).thenReturn(muayenePasif);
        when(sgkAdapter.sgkIndirimOrani("22222222222")).thenReturn(indirimOrani);
        when(odemeRepository.save(any(Odeme.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Odeme sonuc = odemeService.odemeOlustur(11L);

        assertThat(sonuc.getToplamTutar()).isEqualTo(VARSAYILAN_UCRET);
        assertThat(sonuc.getNetTutar()).isCloseTo(VARSAYILAN_UCRET, within(0.001));
    }

    // ─────────────────────────────────────────────
    // 5. Zaten tahsil edilmiş ödeme → ConflictException
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("odemeTahsilat: ödeme zaten ODENDI durumunda → ConflictException fırlatır")
    void odemeTahsilat_zatenOdendi_throwsConflictException() {
        Odeme odendi = Odeme.builder()
                .id(50L)
                .odemeDurumu(OdemeDurumu.ODENDI)
                .toplamTutar(300.0)
                .netTutar(60.0)
                .build();

        when(odemeRepository.findById(50L)).thenReturn(Optional.of(odendi));

        assertThatThrownBy(() -> odemeService.odemeTahsilat(50L, OdemeTipi.NAKIT))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("zaten tahsil edilmiş");

        verify(odemeRepository, never()).save(any());
    }
}
