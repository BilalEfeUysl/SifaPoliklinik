# Klinik Yönetim Sistemi

## Proje Yapısı
- `backend/` — Java Spring Boot (Maven, `pom.xml`)
- `frontend/` — React + Vite (`vite.config.js`)

## Roller ve Yetkiler
- **Admin**: Muayene bilgisi görüntüleme, ödeme görüntüleme, klinik/doktor CRUD, randevu görüntüleme, rol atama, görevli ekleme
- **Kayıt görevlisi**: Hasta CRUD (sorgulama, ekleme, silme vb.) — dashboard yok
- **Randevu görevlisi**: Randevu düzenleme/oluşturma (hasta yazarak arama, klinik→doktor seçimi)
- **Veznedar**: Sadece ödeme işlemleri — dashboard yok
- **Doktor**: Kendi dashboard'u, hasta geçmiş muayeneleri, sadece kendi randevuları

## Token Tasarrufu Kuralları
- Dosya okumadan önce `grep` veya `find` ile hedefi bul
- Büyük dosyaları tamamen okuma — sadece ilgili satır aralığını oku (`offset` + `limit`)
- Paralel tool call kullan — bağımsız işlemleri aynı anda yap
- Açıklama yazma, kod konuşsun
- Özet/sonuç mesajı yazma — diff yeterli
- Gereksiz import/dosya tarama yapma

## Caveman Modu
Kullanıcı **"caveman"** veya **"mağara"** yazarsa:
- Çok kısa, ilkel cevaplar ver
- Türkçe basit kelimeler kullan
- Örnek: "tamam yaptım", "bak şurda", "bu bozuk düzelt"
- Uzun açıklama yapma, teknik terim kullanma
- Sadece sonucu göster, yorum yok

## Geliştirme Notları
- Backend değişikliklerinde sadece ilgili servis/controller dosyalarını oku
- Frontend'de component bazında çalış, tüm `src/` tarama
- Test yazma (istenmedikçe)
- Yorum satırı ekleme
- Mevcut dosyaları düzenle, yeni dosya açma (zorunlu değilse)
