package com.sifa.poliklinik.model.enums;

/**
 * Hastanın SGK (Sosyal Güvenlik Kurumu) durumu.
 */
public enum SGKDurumu {
    AKTIF,    // SGK aktif → %80 indirim uygulanır
    PASIF     // SGK pasif → tam ücret ödenir
}
