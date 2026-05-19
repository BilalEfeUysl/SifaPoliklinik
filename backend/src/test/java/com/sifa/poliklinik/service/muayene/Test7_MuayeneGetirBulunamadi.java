package com.sifa.poliklinik.service.muayene;

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
 * TEST 7: muayeneGetir — kayıt bulunamadı → NotFoundException
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Test 7 - Muayene Kaydı Getirilirken Bulunamadı")
class Test7_MuayeneGetirBulunamadi {

    @Mock private MuayeneRepository muayeneRepository;
    @Mock private RandevuRepository randevuRepository;
    @Mock private OdemeRepository odemeRepository;
    @Mock private SGKAdapter sgkAdapter;

    @InjectMocks private MuayeneService muayeneService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(muayeneService, "varsayilanMuayeneUcreti", 200.0);
    }

    @Test
    @DisplayName("Var olmayan muayene ID'si getirilince NotFoundException fırlatılmalı ve mesajda ID geçmeli")
    void varOlmayanMuayeneId_getirilince_NotFoundException_firlatilmali() {
        long aranacakId = 404L;
        when(muayeneRepository.findById(aranacakId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> muayeneService.muayeneGetir(aranacakId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(String.valueOf(aranacakId));

        // Sadece muayene repository'ye sorgu yapıldı, başka hiçbir şey çağrılmadı
        verify(muayeneRepository, times(1)).findById(aranacakId);
        verifyNoInteractions(randevuRepository, odemeRepository, sgkAdapter);
    }
}
