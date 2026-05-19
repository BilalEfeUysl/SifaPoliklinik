package com.sifa.poliklinik.service.sgk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SGK Adapter - Adapter Design Pattern.
 *
 * Bu sınıf, MockSGKService (Adaptee) ile SGKService (Target) arasında
 * köprü görevi görür. Gelecekte gerçek SGK API'sine geçildiğinde
 * sadece bu adapter değiştirilir.
 *
 * Pattern Yapısı:
 * - Target:  SGKService (interface)
 * - Adaptee: MockSGKService (mock implementasyon)
 * - Adapter: SGKAdapter (bu sınıf)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SGKAdapter implements SGKService {

    private final MockSGKService mockSGKService;

    @Override
    public boolean sgkDurumKontrol(String tcKimlik) {
        log.info("SGKAdapter: TC {} için SGK durum kontrolü yapılıyor...", tcKimlik);
        // MockSGKService'in farklı arayüzünü SGKService arayüzüne dönüştürür
        return mockSGKService.kayitSorgula(tcKimlik);
    }

    @Override
    public double sgkIndirimOrani(String tcKimlik) {
        log.info("SGKAdapter: TC {} için indirim oranı sorgulanıyor...", tcKimlik);
        try {
            double oran = mockSGKService.primIndirimOrani(tcKimlik);
            log.info("SGKAdapter: TC {} indirim oranı={}", tcKimlik, oran);
            return oran;
        } catch (Exception e) {
            log.error("SGKAdapter: TC {} sorgusu başarısız: {}", tcKimlik, e.getMessage(), e);
            throw e;
        }
    }

    public double sgkIndirimOrani(String tcKimlik, boolean forceRefresh) {
        return mockSGKService.primIndirimOrani(tcKimlik, forceRefresh);
    }
}
