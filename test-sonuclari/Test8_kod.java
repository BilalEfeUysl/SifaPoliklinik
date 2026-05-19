package com.sifa.poliklinik.service.muayene;

import com.sifa.poliklinik.dto.MuayeneRequest;
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
 * TEST 8: muayeneGuncelle — tüm alanlar doğru güncellenmeli
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Test 8 - Muayene Kaydı Güncelleme")
class Test8_MuayeneGuncelle {

    @Mock private MuayeneRepository muayeneRepository;
    @Mock private RandevuRepository randevuRepository;
    @Mock private OdemeRepository odemeRepository;
    @Mock private SGKAdapter sgkAdapter;

    @InjectMocks private MuayeneService muayeneService;

    private MuayeneKaydi mevcutMuayene;
    private MuayeneRequest guncelRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(muayeneService, "varsayilanMuayeneUcreti", 200.0);

        Randevu randevu = Randevu.builder()
                .id(80L).durum(RandevuDurumu.TAMAMLANDI).build();

        mevcutMuayene = MuayeneKaydi.builder()
                .id(80L)
                .randevu(randevu)
                .tani("Eski tanı")
                .recete("Eski reçete")
                .rapor("Eski rapor")
                .sevkKurumu(null)
                .build();

        guncelRequest = new MuayeneRequest();
        guncelRequest.setRandevuId(80L);
        guncelRequest.setTani("Güncel tanı: Tip 2 Diyabet");
        guncelRequest.setRecete("Metformin 850 mg, günde 2x");
        guncelRequest.setRapor("HbA1c: 7.2. Diyet ve egzersiz önerildi.");
        guncelRequest.setSevkKurumu("Endokrinoloji Polikliniği");
    }

    @Test
    @DisplayName("muayeneGuncelle çağrısında tüm alanlar (tanı, reçete, rapor, sevk) doğru güncellenmeli")
    void muayeneGuncelle_tumAlanlar_dogruGuncellenmeli() {
        when(muayeneRepository.findById(80L)).thenReturn(Optional.of(mevcutMuayene));
        when(muayeneRepository.save(any(MuayeneKaydi.class))).thenAnswer(inv -> inv.getArgument(0));

        MuayeneKaydi sonuc = muayeneService.muayeneGuncelle(80L, guncelRequest);

        // Dönen nesne doğrulanır
        assertThat(sonuc.getTani()).isEqualTo("Güncel tanı: Tip 2 Diyabet");
        assertThat(sonuc.getRecete()).isEqualTo("Metformin 850 mg, günde 2x");
        assertThat(sonuc.getRapor()).isEqualTo("HbA1c: 7.2. Diyet ve egzersiz önerildi.");
        assertThat(sonuc.getSevkKurumu()).isEqualTo("Endokrinoloji Polikliniği");

        // Repository'ye gönderilen nesne de doğrulanır
        ArgumentCaptor<MuayeneKaydi> captor = ArgumentCaptor.forClass(MuayeneKaydi.class);
        verify(muayeneRepository, times(1)).save(captor.capture());
        MuayeneKaydi kaydedilen = captor.getValue();
        assertThat(kaydedilen.getId()).isEqualTo(80L);
        assertThat(kaydedilen.getTani()).isEqualTo("Güncel tanı: Tip 2 Diyabet");

        // Randevu ve ödeme bu işlemden etkilenmemeli
        verifyNoInteractions(randevuRepository, odemeRepository, sgkAdapter);
    }
}
