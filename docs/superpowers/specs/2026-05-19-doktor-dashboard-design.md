# Doktor Dashboard Tasarım Dokümanı

## Genel Bakış

DOKTOR rolündeki kullanıcılar için ayrı bir dashboard sayfası. Mevcut akışta doktorlar `/muayeneler` sayfasına yönlendiriliyor; bu değiştirilecek ve doktorlar giriş yaptıklarında kendi dashboardlarını görecek.

## Mimari

### Yeni Dosya

- `frontend/src/pages/DoktorDashboardPage.jsx`

### Değiştirilen Dosyalar

- `frontend/src/App.jsx` — `HomeRedirect` bileşeninde DOKTOR için `/muayeneler` yönlendirmesi kaldırılır, ana sayfada `DoktorDashboardPage` render edilir.

## Doktor Tespiti

Login response'unda doktorId gelmediğinden, MuayenePage ile aynı pattern kullanılır:

1. `doktorAPI.getAll()` ile tüm doktorlar çekilir
2. `d.kullanici?.id === user?.id` veya `d.kullanici?.email === user?.email` ile eşleşme bulunur
3. Bulunan kaydın `id`'si sonraki API çağrılarında `doktorId` olarak kullanılır

## Bileşenler

### İstatistik Kartları (3 adet)

| Kart | API | Hesaplama |
|---|---|---|
| Bugün Bekleyen | `randevuAPI.getByDoktor(doktorId)` çıktısından bugün + BEKLEYEN filtresi | yerel hesaplama |
| Toplam Randevu | `randevuAPI.getByDoktor(doktorId)` | response.data.length |
| Toplam Muayene | `muayeneAPI.getByDoktor(doktorId)` | response.data.length |

### Bugünkü Randevular (sol kolon)

- `randevuAPI.getByDoktor(doktorId)` çıktısından bugünün tarihine göre filtrele
- Tarihe göre artan sıralama
- **BEKLEYEN** durum: mavi "Muayeneye Başla" butonu → `/muayeneler` sayfasına yönlendir (react-router `useNavigate`)
- **TAMAMLANDI** durum: gri "Tamamlandı" badge, etkileşim yok
- **İPTAL** durum: kırmızı "İptal" badge

### Son Muayeneler (sağ kolon)

- `muayeneAPI.getByDoktor(doktorId)` çıktısından son 5 kayıt (tersten)
- Her satır: hasta adı soyadı, tarih, tanı özeti

## Veri Akışı

```
component mount
  → doktorAPI.getAll()         [doktor kaydını bul]
  → Promise.all([
      randevuAPI.getByDoktor(), [toplam + bugün filtresi + bekleyen sayımı]
      muayeneAPI.getByDoktor()  [toplam + son muayeneler]
    ])
  → state güncelle → render
```

## Routing Değişikliği

`App.jsx` `HomeRedirect`:

```js
// Önce (kaldırılacak)
if (user?.rol === 'DOKTOR') return <Navigate to="/muayeneler" replace />;

// Sonra: DOKTOR ana sayfada kalır, DoktorDashboardPage render edilir
// HomeRedirect içinde rol kontrolü ile DoktorDashboardPage döndürülür
```

Doktor artık `/` path'inde `DoktorDashboardPage` görür. `/muayeneler` sayfasına "Muayeneye Başla" butonu veya sidebar üzerinden erişebilir.
