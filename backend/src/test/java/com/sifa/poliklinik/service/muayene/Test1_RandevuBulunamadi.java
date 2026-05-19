package com.sifa.poliklinik.service.muayene;

import com.sifa.poliklinik.dto.MuayeneRequest;
import com.sifa.poliklinik.exception.NotFoundException;
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
 * TEST 1: muayeneKaydet — randevu bulunamadı → NotFoundException
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Test 1 - Randevu Bulunamadı")
class Test1_RandevuBulunamadi {

    @Mock private MuayeneRepository muayeneRepository;
    @Mock private RandevuRepository randevuRepository;
    @Mock private OdemeRepository odemeRepository;
    @Mock private SGKAdapter sgkAdapter;

    @InjectMocks private MuayeneService muayeneService;

    private MuayeneRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(muayeneService, "varsayilanMuayeneUcreti", 200.0);

        request = new MuayeneRequest();
        request.setRandevuId(999L);
        request.setTani("Herhangi bir tanı");
    }

    @Test
    @DisplayName("Var olmayan randevu ID'si ile muayene kaydı oluşturulunca NotFoundException fırlatılmalı")
    void varOlmayanRandevuIcin_NotFoundException_firlatilmali() {
        // Hiçbir randevu dönmüyor
        when(randevuRepository.findById(999L)).thenReturn(Optional.empty());

        // NotFoundException fırlatılmalı ve mesajda ID geçmeli
        assertThatThrownBy(() -> muayeneService.muayeneKaydet(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");

        // Randevu arandı
        verify(randevuRepository, times(1)).findById(999L);

        // Muayene veya ödeme kesinlikle kaydedilmemeli
        verifyNoInteractions(muayeneRepository);
        verifyNoInteractions(odemeRepository);
        verifyNoInteractions(sgkAdapter);
    }
}
