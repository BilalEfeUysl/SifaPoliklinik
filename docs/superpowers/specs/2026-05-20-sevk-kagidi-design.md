# Sevk Kâğıdı Yazdır — Tasarım Dokümanı

**Tarih:** 2026-05-20  
**Durum:** Onaylandı

## Özet

Muayene kaydında `sevkKurumu` alanı dolu olduğunda doktor veya yönetici "Sevk Kâğıdı Yazdır" butonuna basarak tarayıcının yazdırma/PDF diyaloğunu açabilir. Yalnızca frontend değişikliği gerekir; backend ve veritabanı dokunulmaz.

## Kapsam

- Yeni bir `SevkKagidiModal` bileşeni (`MuayenePage.jsx` içinde)
- İki tetikleyici nokta: görüntüleme modalinin footer'ı + muayene kaydedildikten sonra
- `@media print` CSS kuralları (`index.css`)
- Sıfır yeni bağımlılık, sıfır backend değişikliği

## Bileşen Tasarımı

### `SevkKagidiModal`

`MuayenePage.jsx` içinde tanımlanacak yerel bileşen. Props:
- `muayene` — mevcut `selectedMuayene` veya kayıt sonrası dönen muayene nesnesi
- `onClose` — modalı kapat

İçerik (A şablonu — Klasik Resmi Belge):
1. Kurum başlığı: "ŞIFA POLİKLİNİĞİ" + "SEVK KÂĞIDI" alt başlığı, teal alt çizgi
2. Hasta bilgileri ızgarası: ad-soyad, TC kimlik, doğum tarihi, sevk tarihi
3. Sevk edilen kurum kutusu
4. Tanı / sevk nedeni kutusu
5. Doktor imza alanı (ad, unvan)
6. Footer: "Yazdır" butonu (`window.print()`) + "Kapat" butonu

### Tetikleyici Noktalar

**1. Görüntüleme modalı (admin + doktor):**  
`showViewModal` footer'ına "Sevk Kâğıdı Yazdır" butonu eklenir. Yalnızca `selectedMuayene.sevkKurumu` dolu olduğunda görünür.

**2. Muayene kaydı sonrası:**  
`handleSubmit` başarılı olduğunda `showSevkModal` state'i `true` yapılır (sevkKurumu doluysa). Kullanıcıya kayıt başarılı + sevk kâğıdı seçeneği sunulur.

## CSS (`@media print`)

```css
@media print {
  body > *:not(#sevk-print-root) { display: none; }
  #sevk-print-root { display: block !important; }
}
```

Baskıya sadece `#sevk-print-root` id'li sevk belgesi gider; modal overlay, navbar ve diğer tüm sayfa içeriği bastırılır.

## Veri Akışı

Yeni API çağrısı yok. Mevcut `selectedMuayene` nesnesi tüm gerekli alanları içeriyor:
- `randevu.hasta.ad/soyad/tcKimlik/dogumTarihi`
- `randevu.doktor.unvan/kullanici.ad/kullanici.soyad/klinik.ad`
- `tani`
- `sevkKurumu`
- `muayeneTarihi`

## Kısıtlamalar

- Sevk kâğıdı butonu yalnızca `sevkKurumu` dolu olduğunda görünür
- Hasta doğum tarihi `Hasta` modelinde yoksa o satır gösterilmez (graceful fallback)
- PDF üretimi tarayıcının "PDF olarak kaydet" özelliğine bırakılır; ayrı PDF kütüphanesi eklenmez
