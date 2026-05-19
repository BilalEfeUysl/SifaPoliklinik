package com.sifa.poliklinik.service.muayene;

import com.sifa.poliklinik.dto.MuayeneRequest;
import com.sifa.poliklinik.model.*;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TEST 4: muayeneKaydet — başarılı → randevu durumu TAMAMLANDI olmalı
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Test 4 - Başarılı Muayene Kaydı ve Randevu Durumu Güncellemesi")
class Test4_BasariliMuayeneKaydi {

    @Mock private MuayeneRepository muayeneRepository;
    @Mock private RandevuRepository randevuRepository;
    @Mock private OdemeRepository odemeRepository;
    @Mock private SGKAdapter sgkAdapter;

    @InjectMocks private MuayeneService muayeneService;

    private Randevu bekleyenRandevu;
    private MuayeneRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(muayeneService, "varsayilanMuayeneUcreti", 200.0);

        Hasta hasta = Hasta.builder().id(1L).tcKimlik("33333333333")
                .ad("Mehmet").soyad("Çelik").sgkDurumu(SGKDurumu.AKTIF).build();

        Kullanici kullanici = Kullanici.builder().id(3L).ad("Uzm").soyad("Kaya")
                .email("uzm@sifa.com").sifre("pass").build();
        Klinik klinik = Klinik.builder().id(3L).ad("Nöroloji").muayeneUcreti(400.0).build();
        Doktor doktor = Doktor.builder().id(3L).kullanici(kullanici).klinik(klinik).build();

        bekleyenRandevu = Randevu.builder()
                .id(30L).hasta(hasta).doktor(doktor)
                .durum(RandevuDurumu.BEKLIYOR)
                .build();

        request = new MuayeneRequest();
        request.setRandevuId(30L);
        request.setTani("Migren tanısı");
        request.setRecete("İbuprofen 400 mg, günde 3x");
        request.setRapor("Kronik migren. İstirahat önerildi.");
        request.setSevkKurumu(null);
    }

    @Test
    @DisplayName("Başarılı muayene kaydında randevu durumu BEKLIYOR'dan TAMAMLANDI'ya geçmeli")
    void basariliKayit_randevuDurumu_TAMAMLANDI_olmali() {
        MuayeneKaydi kaydedilen = MuayeneKaydi.builder()
                .id(30L).randevu(bekleyenRandevu)
                .tani(request.getTani()).recete(request.getRecete()).rapor(request.getRapor())
                .build();

        when(randevuRepository.findById(30L)).thenReturn(Optional.of(bekleyenRandevu));
        when(muayeneRepository.save(any(MuayeneKaydi.class))).thenReturn(kaydedilen);
        when(odemeRepository.findByMuayeneKaydiId(30L)).thenReturn(Optional.empty());
        when(sgkAdapter.sgkIndirimOrani("33333333333")).thenReturn(0.80);
        when(odemeRepository.save(any(Odeme.class))).thenAnswer(inv -> inv.getArgument(0));

        MuayeneKaydi sonuc = muayeneService.muayeneKaydet(request);

        // Dönen muayene doğrulanır
        assertThat(sonuc).isNotNull();
        assertThat(sonuc.getId()).isEqualTo(30L);
        assertThat(sonuc.getTani()).isEqualTo("Migren tanısı");

        // Randevu TAMAMLANDI durumuna geçtiği doğrulanır
        ArgumentCaptor<Randevu> randevuCaptor = ArgumentCaptor.forClass(Randevu.class);
        verify(randevuRepository, times(1)).save(randevuCaptor.capture());
        assertThat(randevuCaptor.getValue().getDurum()).isEqualTo(RandevuDurumu.TAMAMLANDI);
        assertThat(randevuCaptor.getValue().getId()).isEqualTo(30L);

        // Muayene tam olarak bir kez kaydedilmeli
        verify(muayeneRepository, times(1)).save(any(MuayeneKaydi.class));
    }
}
