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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TEST 6: muayeneKaydet — ödeme zaten mevcutsa ikinci ödeme oluşturulmamalı
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Test 6 - Mükerrer Ödeme Önleme")
class Test6_MukerrerOdemeOnleme {

    @Mock private MuayeneRepository muayeneRepository;
    @Mock private RandevuRepository randevuRepository;
    @Mock private OdemeRepository odemeRepository;
    @Mock private SGKAdapter sgkAdapter;

    @InjectMocks private MuayeneService muayeneService;

    private Randevu randevu;
    private MuayeneRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(muayeneService, "varsayilanMuayeneUcreti", 200.0);

        Hasta hasta = Hasta.builder().id(6L).tcKimlik("66666666666")
                .ad("Hasan").soyad("Güneş").sgkDurumu(SGKDurumu.PASIF).build();

        Kullanici kullanici = Kullanici.builder().id(6L).ad("Op").soyad("Bulut")
                .email("op@sifa.com").sifre("pass").build();
        Klinik klinik = Klinik.builder().id(6L).ad("Göz").muayeneUcreti(350.0).build();
        Doktor doktor = Doktor.builder().id(6L).kullanici(kullanici).klinik(klinik).build();

        randevu = Randevu.builder()
                .id(60L).hasta(hasta).doktor(doktor)
                .durum(RandevuDurumu.BEKLIYOR)
                .build();

        request = new MuayeneRequest();
        request.setRandevuId(60L);
        request.setTani("Katarakt tespiti");
    }

    @Test
    @DisplayName("Bu muayene için ödeme zaten kaydedilmişse yeni ödeme oluşturulmamalı, SGKAdapter çağrılmamalı")
    void odemeZatenVar_ikincOdeme_olusturulmamali() {
        MuayeneKaydi kaydedilen = MuayeneKaydi.builder()
                .id(60L).randevu(randevu).tani(request.getTani()).build();

        // Bu muayene için zaten bir ödeme mevcut
        Odeme mevcutOdeme = Odeme.builder().id(777L).build();

        when(randevuRepository.findById(60L)).thenReturn(Optional.of(randevu));
        when(muayeneRepository.save(any(MuayeneKaydi.class))).thenReturn(kaydedilen);
        when(odemeRepository.findByMuayeneKaydiId(60L)).thenReturn(Optional.of(mevcutOdeme));

        muayeneService.muayeneKaydet(request);

        // Yeni ödeme save çağrısı hiç yapılmamalı
        verify(odemeRepository, never()).save(any(Odeme.class));

        // SGKAdapter hiç çağrılmamalı (gereksiz dış servis tüketimi olur)
        verifyNoInteractions(sgkAdapter);
    }
}
