package com.sifa.poliklinik.service;

import com.sifa.poliklinik.model.AuditLog;
import com.sifa.poliklinik.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void kaydet(String islem, String kullaniciEmail, Long etkilenenKayitId, String aciklama) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .islem(islem)
                    .kullaniciEmail(kullaniciEmail)
                    .etkilenenKayitId(etkilenenKayitId)
                    .aciklama(aciklama)
                    .tarih(LocalDateTime.now())
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Audit log kaydedilemedi: islem={}, hata={}", islem, e.getMessage());
        }
    }
}
