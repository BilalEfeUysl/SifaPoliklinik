package com.sifa.poliklinik.service.sgk;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MockSGKService {

    private final Map<String, Boolean> sgkCache = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public boolean kayitSorgula(String tcKimlik) {
        return kayitSorgula(tcKimlik, false);
    }

    public boolean kayitSorgula(String tcKimlik, boolean forceRefresh) {
        if (!forceRefresh && sgkCache.containsKey(tcKimlik)) {
            log.info("MockSGK: TC {} cache'den döndürüldü - {}", tcKimlik, sgkCache.get(tcKimlik) ? "AKTİF" : "PASİF");
            return sgkCache.get(tcKimlik);
        }
        log.info("MockSGK: TC {} için dış SGK sunucusuna istek gönderiliyor...", tcKimlik);
        try {
            long gecikme = 300 + (long)(random.nextDouble() * 500);
            Thread.sleep(gecikme);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean aktif = random.nextDouble() < 0.50;
        sgkCache.put(tcKimlik, aktif);
        log.info("MockSGK: TC {} - SGK yanıtı alındı: {}", tcKimlik, aktif ? "AKTİF" : "PASİF");
        return aktif;
    }

    public double primIndirimOrani(String tcKimlik) {
        return kayitSorgula(tcKimlik) ? 0.80 : 0.0;
    }

    public double primIndirimOrani(String tcKimlik, boolean forceRefresh) {
        return kayitSorgula(tcKimlik, forceRefresh) ? 0.80 : 0.0;
    }
}
