package com.sifa.poliklinik.service.muayene;

import com.sifa.poliklinik.dto.MuayeneRequest;
import com.sifa.poliklinik.model.*;
import com.sifa.poliklinik.model.enums.OdemeDurumu;
import com.sifa.poliklinik.model.enums.RandevuDurumu;
import com.sifa.poliklinik.model.enums.SGKDurumu;
import com.sifa.poliklinik.repository.MuayeneRepository;
import com.sifa.poliklinik.repository.OdemeRepository;
import com.sifa.poliklinik.repository.RandevuRepository;
import com.sifa.poliklinik.service.MuayeneService;
import com.sifa.poliklinik.service.sgk.SGKAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TEST 5: muayeneKaydet — SGK %80 indirim doğru hesaplanmalı
 * Klinik ücreti: 500 TL → SGK indirimi: 400 TL → Net tutar: 100 TL
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Test 5 - SGK Aktif Hasta İçin %80 İndirim Hesabı")
class Test5_SgkIndirimHesabi {

    @Mock private MuayeneRepository muayeneRepository;
    @Mock private RandevuRepository randevuRepository;
    @Mock private OdemeRepository odemeRepository;
    @Mock private SGKAdapter sgkAdapter;

    @InjectMocks private MuayeneService muayeneService;

    private static final double KLINIK_UCRETI    = 500.0;
    private static final double SGK_INDIRIM_ORANI = 0.80;
    private static final double BEKLENEN_INDIRIM  = 400.0;   // 500 * 0.80
    private static final double BEKLENEN_NET      = 100.0;   // 500 - 400

    private Randevu randevu;
    private MuayeneRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(muayeneService, "varsayilanMuayeneUcreti", 200.0);

        Hasta hasta = Hasta.builder().id(5L).tcKimlik("55555555555")
                .ad("Fatma").soyad("Öz").sgkDurumu(SGKDurumu.AKTIF).build();

        Kullanici kullanici = Kullanici.builder().id(5L).ad("Doç").soyad("Arslan")
                .email("doc@sifa.com").sifre("pass").build();
        Klinik klinik = Klinik.builder().id(5L).ad("Ortopedi").muayeneUcreti(KLINIK_UCRETI).build();
        Doktor doktor = Doktor.builder().id(5L).kullanici(kullanici).klinik(klinik).build();

        randevu = Randevu.builder()
                .id(50L).hasta(hasta).doktor(doktor)
                .durum(RandevuDurumu.BEKLIYOR)
                .build();

        request = new MuayeneRequest();
        request.setRandevuId(50L);
        request.setTani("Diz protezi önerisi");
    }

    @Test
    @DisplayName("SGK aktif hasta için ödeme tutarları doğru hesaplanmalı: 500 → indirim 400 → net 100")
    void sgkAktifHasta_odeme_dogruHesaplanmali() {
        MuayeneKaydi kaydedilen = MuayeneKaydi.builder()
                .id(50L).randevu(randevu).tani(request.getTani()).build();

        when(randevuRepository.findById(50L)).thenReturn(Optional.of(randevu));
        when(muayeneRepository.save(any(MuayeneKaydi.class))).thenReturn(kaydedilen);
        when(odemeRepository.findByMuayeneKaydiId(50L)).thenReturn(Optional.empty());
        when(sgkAdapter.sgkIndirimOrani("55555555555")).thenReturn(SGK_INDIRIM_ORANI);
        when(odemeRepository.save(any(Odeme.class))).thenAnswer(inv -> inv.getArgument(0));

        muayeneService.muayeneKaydet(request);

        // Ödeme nesnesinin alanları doğrulanır
        ArgumentCaptor<Odeme> odemeCaptor = ArgumentCaptor.forClass(Odeme.class);
        verify(odemeRepository, times(1)).save(odemeCaptor.capture());
        Odeme odeme = odemeCaptor.getValue();

        assertThat(odeme.getToplamTutar()).isEqualTo(KLINIK_UCRETI);
        assertThat(odeme.getSgkIndirimi()).isCloseTo(BEKLENEN_INDIRIM, within(0.001));
        assertThat(odeme.getNetTutar()).isCloseTo(BEKLENEN_NET, within(0.001));
        assertThat(odeme.getOdemeDurumu()).isEqualTo(OdemeDurumu.BEKLIYOR);
        assertThat(odeme.getHasta().getTcKimlik()).isEqualTo("55555555555");

        // SGKAdapter doğru TC ile çağrıldı mı?
        verify(sgkAdapter, times(1)).sgkIndirimOrani("55555555555");
    }
}
