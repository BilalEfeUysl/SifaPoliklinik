# Şifa Poliklinik - Sınıf Düzeyinde İzlenebilirlik Tablosu 

> **Amaç:** Yazılım geliştirme süreci içerisinde gereksinimlerin gerçeklenmesini izlemek.
> - **G1, G2, …** → Gereksinimler 
> - **B1, B2, …** → Sisteme çeşitli bakış açıları: Sınıflar, Modüller, Paketler
> - **✓** → İlgili sınıf bu gereksinimi karşılar / gerçekler

---

## Gereksinim (G) Listesi

| ID | Gereksinim Adı |
|:---|:---|
| **G1** | Kullanıcı Girişi ve Kimlik Doğrulama |
| **G2** | Rol Bazlı Erişim Kontrolü (RBAC) |
| **G3** | Hasta Kayıt ve Düzenleme |
| **G4** | SGK Doğrulama / Sorgulama |
| **G5** | Randevu Oluşturma |
| **G6** | Randevu Çakışma Kontrolü  |
| **G7** | Borçlu Hastaya Randevu Engeli |
| **G8** | Muayene Kaydı Oluşturma |
| **G9** | Reçete ve Tahlil Yazımı |
| **G10** | Ödeme / Tahsilat İşlemi |
| **G11** | Ödeme İptali |
| **G12** | Dashboard ve İstatistik Görüntüleme |
| **G13** | Klinik ve Doktor Yönetimi |
| **G14** | Sistem İçi İşlem Kayıtları (Audit Log) |

---

## Bileşen (B) Listesi

| ID | Sınıf / Modül | Katman |
|:---|:---|:---|
| **B1** | `Kullanici.java` | Model |
| **B2** | `Hasta.java` | Model |
| **B3** | `Randevu.java` | Model |
| **B4** | `Doktor.java` | Model |
| **B5** | `Klinik.java` | Model |
| **B6** | `MuayeneKaydi.java` | Model |
| **B7** | `Odeme.java` | Model |
| **B8** | `HastaService.java` | Servis |
| **B9** | `RandevuService.java` | Servis |
| **B10** | `MuayeneService.java` | Servis |
| **B11** | `OdemeService.java` | Servis |
| **B12** | `AuthController.java` | Controller |
| **B13** | `HastaController.java` | Controller |
| **B14** | `RandevuController.java` | Controller |
| **B15** | `MuayeneController.java` | Controller |
| **B16** | `OdemeController.java` | Controller |
| **B17** | `YoneticiController.java` | Controller |
| **B18** | `SecurityConfig.java` | Güvenlik |
| **B19** | `JwtAuthFilter.java` | Güvenlik |
| **B20** | `SGKAdapter.java` | Entegrasyon |
| **B21** | `LoginPage.jsx` | Frontend |
| **B22** | `HastaListPage.jsx` | Frontend |
| **B23** | `RandevuListPage.jsx` | Frontend |
| **B24** | `MuayenePage.jsx` | Frontend |
| **B25** | `OdemePage.jsx` | Frontend |
| **B26** | `DashboardPage.jsx` | Frontend |
| **B27** | `YoneticiPage.jsx` | Frontend |
| **B28** | `AuditLog.java` | Model |
| **B29** | `AuditLogService.java` | Servis |
| **B30** | `GenelController.java` | Controller |
| **B31** | `GlobalExceptionHandler.java` | Controller |
| **B32** | `DoktorDashboardPage.jsx` | Frontend |

---

## İzlenebilirlik Matrisi

### Bölüm 1 — Model Katmanı (B1 – B7)

|  | B1 `Kullanici` | B2 `Hasta` | B3 `Randevu` | B4 `Doktor` | B5 `Klinik` | B6 `MuayeneKaydi` | B7 `Odeme` | B28 `AuditLog` |
|:---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| **G1** Kullanıcı Girişi | ✓ | | | | | | | |
| **G2** RBAC | ✓ | | | | | | | |
| **G3** Hasta Kayıt | | ✓ | | | | | | |
| **G4** SGK Doğrulama | | ✓ | | | | | | |
| **G5** Randevu Oluşturma | | ✓ | ✓ | ✓ | | | | |
| **G6** Çakışma Kontrolü | | | ✓ | ✓ | | | | |
| **G7** Borç Engeli | | ✓ | ✓ | | | | ✓ | |
| **G8** Muayene Kaydı | | ✓ | ✓ | ✓ | | ✓ | | |
| **G9** Reçete/Tahlil | | | | | | ✓ | | |
| **G10** Ödeme/Tahsilat | | ✓ | | | | ✓ | ✓ | |
| **G11** Ödeme İptali | | | | | | | ✓ | |
| **G12** Dashboard | | ✓ | ✓ | ✓ | ✓ | | ✓ | |
| **G13** Klinik/Doktor Yön. | | | | ✓ | ✓ | | | |
| **G14** Audit Log | | | | | | | | ✓ |

---

### Bölüm 2 — Servis Katmanı (B8 – B11)

|  | B8 `HastaService` | B9 `RandevuService` | B10 `MuayeneService` | B11 `OdemeService` | B29 `AuditLogService` |
|:---|:---:|:---:|:---:|:---:|:---:|
| **G1** Kullanıcı Girişi | | | | | |
| **G2** RBAC | | | | | |
| **G3** Hasta Kayıt | ✓ | | | | |
| **G4** SGK Doğrulama | ✓ | | | | |
| **G5** Randevu Oluşturma | ✓ | ✓ | | | |
| **G6** Çakışma Kontrolü | | ✓ | | | |
| **G7** Borç Engeli | ✓ | ✓ | | ✓ | |
| **G8** Muayene Kaydı | | | ✓ | | |
| **G9** Reçete/Tahlil | | | ✓ | | |
| **G10** Ödeme/Tahsilat | | | ✓ | ✓ | |
| **G11** Ödeme İptali | | | | ✓ | |
| **G12** Dashboard | ✓ | ✓ | | ✓ | |
| **G13** Klinik/Doktor Yön. | | | | | |
| **G14** Audit Log | ✓ | ✓ | | ✓ | ✓ |

---

### Bölüm 3 — Controller Katmanı (B12 – B17)

|  | B12 `AuthController` | B13 `HastaController` | B14 `RandevuController` | B15 `MuayeneController` | B16 `OdemeController` | B17 `YoneticiController` | B30 `GenelController` | B31 `GlobalExceptionHandler` |
|:---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| **G1** Kullanıcı Girişi | ✓ | | | | | | | |
| **G2** RBAC | | | | | | ✓ | | |
| **G3** Hasta Kayıt | | ✓ | | | | | | |
| **G4** SGK Doğrulama | | ✓ | | | | | | |
| **G5** Randevu Oluşturma | | | ✓ | | | | ✓ | |
| **G6** Çakışma Kontrolü | | | ✓ | | | | | |
| **G7** Borç Engeli | | | ✓ | | | | | |
| **G8** Muayene Kaydı | | | | ✓ | | | | |
| **G9** Reçete/Tahlil | | | | ✓ | | | | |
| **G10** Ödeme/Tahsilat | | | | | ✓ | | | |
| **G11** Ödeme İptali | | | | | ✓ | | | |
| **G12** Dashboard | | | | | | ✓ | | |
| **G13** Klinik/Doktor Yön. | | | | | | ✓ | ✓ | |
| **G14** Audit Log | | | | | | | | |

---

### Bölüm 4 — Güvenlik & Entegrasyon Katmanı (B18 – B20)

|  | B18 `SecurityConfig` | B19 `JwtAuthFilter` | B20 `SGKAdapter` |
|:---|:---:|:---:|:---:|
| **G1** Kullanıcı Girişi | ✓ | ✓ | |
| **G2** RBAC | ✓ | ✓ | |
| **G3** Hasta Kayıt | | | |
| **G4** SGK Doğrulama | | | ✓ |
| **G5** Randevu Oluşturma | ✓ | | |
| **G6** Çakışma Kontrolü | | | |
| **G7** Borç Engeli | | | |
| **G8** Muayene Kaydı | ✓ | | |
| **G9** Reçete/Tahlil | ✓ | | |
| **G10** Ödeme/Tahsilat | ✓ | | |
| **G11** Ödeme İptali | ✓ | | |
| **G12** Dashboard | ✓ | | |
| **G13** Klinik/Doktor Yön. | ✓ | | |
| **G14** Audit Log | ✓ | | |

---

### Bölüm 5 — Frontend Katmanı (B21 – B27)

|  | B21 `LoginPage` | B22 `HastaListPage` | B23 `RandevuListPage` | B24 `MuayenePage` | B25 `OdemePage` | B26 `DashboardPage` | B27 `YoneticiPage` | B32 `DoktorDashboardPage` |
|:---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| **G1** Kullanıcı Girişi | ✓ | | | | | | | |
| **G2** RBAC | ✓ | | | | | | ✓ | |
| **G3** Hasta Kayıt | | ✓ | | | | | | |
| **G4** SGK Doğrulama | | ✓ | | | | | | |
| **G5** Randevu Oluşturma | | | ✓ | | | | | |
| **G6** Çakışma Kontrolü | | | ✓ | | | | | |
| **G7** Borç Engeli | | | ✓ | | | | | |
| **G8** Muayene Kaydı | | | | ✓ | | | | ✓ |
| **G9** Reçete/Tahlil | | | | ✓ | | | | ✓ |
| **G10** Ödeme/Tahsilat | | | | | ✓ | | | |
| **G11** Ödeme İptali | | | | | ✓ | | | |
| **G12** Dashboard | | | | | | ✓ | ✓ | ✓ |
| **G13** Klinik/Doktor Yön. | | | | | | | ✓ | |
| **G14** Audit Log | | | | | | | | |

---

*Not: Bu tablo, Yazılım Mühendisliği dersindeki İzlenebilirlik Tablosu  formatına uygun olarak hazırlanmıştır. G = Gereksinimler, B = Sisteme bakış açıları . ✓ işareti, ilgili sınıfın o gereksinimi gerçeklediğini gösterir.*
