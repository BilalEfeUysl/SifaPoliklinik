package com.sifa.poliklinik.service.muayene;

import com.sifa.poliklinik.dto.MuayeneRequest;
import com.sifa.poliklinik.exception.ConflictException;
import com.sifa.poliklinik.model.*;
import com.sifa.poliklinik.model.enums.RandevuDurumu;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * TEST 3: muayeneKaydet — zaten tamamlanmış randevu → ConflictException
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Test 3 - Zaten Tamamlanmış Randevuya Tekrar Muayene")
class Test3_TamamlanmisRandevuyaMuayene {

    @Mock private MuayeneRepository muayeneRepository;
    @Mock private RandevuRepository randevuRepository;
    @Mock private OdemeRepository odemeRepository;
    @Mock private SGKAdapter sgkAdapter;

    @InjectMocks private MuayeneService muayeneService;

    private Randevu tamamlanmisRandevu;
    private MuayeneRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(muayeneService, "varsayilanMuayeneUcreti", 200.0);

        Hasta hasta = Hasta.builder().id(1L).tcKimlik("11111111111").ad("Ayşe").soyad("Kara").build();

        Kullanici kullanici = Kullanici.builder().id(2L).ad("Prof").soyad("Demir")
                .email("prof@sifa.com").sifre("pass").build();
        Klinik klinik = Klinik.builder().id(2L).ad("Kardiyoloji").muayeneUcreti(500.0).build();
        Doktor doktor = Doktor.builder().id(2L).kullanici(kullanici).klinik(klinik).build();

        tamamlanmisRandevu = Randevu.builder()
                .id(20L).hasta(hasta).doktor(doktor)
                .durum(RandevuDurumu.TAMAMLANDI)
                .build();

        request = new MuayeneRequest();
        request.setRandevuId(20L);
        request.setTani("Yeni tanı denemesi");
    }

    @Test
    @DisplayName("TAMAMLANDI durumundaki randevu için muayene tekrarında ConflictException fırlatılmalı")
    void tamamlanmisRandevu_muayeneKaydedilince_ConflictException_firlatilmali() {
        when(randevuRepository.findById(20L)).thenReturn(Optional.of(tamamlanmisRandevu));

        assertThatThrownBy(() -> muayeneService.muayeneKaydet(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("zaten muayene kaydı mevcut");

        // Randevu durumu değiştirilmemeli — zaten TAMAMLANDI
        verify(randevuRepository, never()).save(any());
        verifyNoInteractions(muayeneRepository);
        verifyNoInteractions(odemeRepository);
        verifyNoInteractions(sgkAdapter);
    }
}
