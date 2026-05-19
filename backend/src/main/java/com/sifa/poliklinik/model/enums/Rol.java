package com.sifa.poliklinik.model.enums;

/**
 * Sistem kullanıcı rolleri.
 * Her rol farklı yetkilere sahiptir.
 */
public enum Rol {
    KAYIT_GOREVLISI,    // Hasta kayıt işlemleri
    RANDEVU_GOREVLISI,  // Randevu yönetimi
    DOKTOR,             // Muayene ve reçete
    VEZNEDAR,           // Ödeme işlemleri
    YONETICI            // Tüm yetkiler
}
