package com.sifa.poliklinik.service.muayene;

import com.sifa.poliklinik.dto.MuayeneRequest;
import com.sifa.poliklinik.exception.BusinessRuleException;
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
 * TEST 2: muayeneKaydet — iptal edilmiş randevu → BusinessRuleException
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Test 2 - İptal Edilmiş Randevuya Muayene Kaydı")
class Test2_IptalRandevuyaMuayene {

    @Mock private MuayeneRepository muayeneRepository;
    @Mock private RandevuRepository randevuRepository;
    @Mock private OdemeRepository odemeRepository;
    @Mock private SGKAdapter sgkAdapter;

    @InjectMocks private MuayeneService muayeneService;

    private Randevu iptalRandevu;
    private MuayeneRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(muayeneService, "varsayilanMuayeneUcreti", 200.0);

        Hasta hasta = Hasta.builder().id(1L).tcKimlik("12345678901").ad("Ali").soyad("Yıldız").build();

        Kullanici kullanici = Kullanici.builder().id(1L).ad("Dr").soyad("Smith")
                .email("dr@sifa.com").sifre("pass").build();
        Klinik klinik = Klinik.builder().id(1L).ad("Dahiliye").muayeneUcreti(300.0).build();
        Doktor doktor = Doktor.builder().id(1L).kullanici(kullanici).klinik(klinik).build();

        iptalRandevu = Randevu.builder()
                .id(10L).hasta(hasta).doktor(doktor)
                .durum(RandevuDurumu.IPTAL)
                .build();

        request = new MuayeneRequest();
        request.setRandevuId(10L);
        request.setTani("Test tanı");
    }

    @Test
    @DisplayName("İptal edilmiş randevu için muayene kaydı girişiminde BusinessRuleException fırlatılmalı")
    void iptalRandevu_muayeneKaydedilince_BusinessRuleException_firlatilmali() {
        when(randevuRepository.findById(10L)).thenReturn(Optional.of(iptalRandevu));

        assertThatThrownBy(() -> muayeneService.muayeneKaydet(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("İptal edilmiş");

        // Randevu durumu değiştirilmemeli
        verify(randevuRepository, never()).save(any());
        // Muayene ve ödeme kesinlikle oluşturulmamalı
        verifyNoInteractions(muayeneRepository);
        verifyNoInteractions(odemeRepository);
        verifyNoInteractions(sgkAdapter);
    }
}
