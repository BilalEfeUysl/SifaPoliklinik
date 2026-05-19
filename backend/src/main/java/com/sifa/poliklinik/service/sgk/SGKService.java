package com.sifa.poliklinik.service.sgk;

/**
 * SGK (Sosyal Güvenlik Kurumu) servisi arayüzü.
 * Adapter Pattern'in "Target" arayüzü.
 */
public interface SGKService {

    /**
     * Hastanın SGK durumunu kontrol eder.
     * @param tcKimlik Hastanın TC Kimlik numarası
     * @return SGK aktif ise true, pasif ise false
     */
    boolean sgkDurumKontrol(String tcKimlik);

    /**
     * SGK indirim oranını döner.
     * @param tcKimlik Hastanın TC Kimlik numarası
     * @return İndirim oranı (0.0 - 1.0 arası)
     */
    double sgkIndirimOrani(String tcKimlik);
}
