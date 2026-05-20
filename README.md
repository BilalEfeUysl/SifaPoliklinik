# Şifa Poliklinik Bilgi Sistemi

Özel poliklinik için geliştirilmiş rol tabanlı klinik yönetim sistemi.

**Stack:** Java 17 + Spring Boot 3 · React 18 + Vite · H2 (geliştirme) / PostgreSQL (üretim) · JWT

---

## Gereksinimler

| Araç | Sürüm |
|------|-------|
| Java | 17+ |
| Maven | 3.8+ |
| Node.js | 18+ |

> Maven kurulu değilse: https://maven.apache.org/download.cgi  
> Java kurulu değilse: https://adoptium.net

---

## Kurulum ve Çalıştırma

### 1. Repoyu klonla

```bash
git clone https://github.com/BilalEfeUysl/SifaPoliklinik.git
cd SifaPoliklinik
```

### 2. Backend'i başlat

```bash
cd backend
mvn spring-boot:run
```

İlk çalıştırmada Maven bağımlılıkları indirir (~1-2 dk). Backend `http://localhost:8080` adresinde çalışmaya başlar.

> Veritabanı dosyaları (`backend/data/`) repoda hazır gelir — hiçbir kurulum gerekmez, uygulama ayağa kalkar kalkmaz örnek veriler hazırdır.

### 3. Frontend'i başlat (yeni terminal)

```bash
cd frontend
npm install
npm run dev
```

Uygulama `http://localhost:5173` adresinde açılır.

---

## Test Kullanıcıları

Uygulama aşağıdaki hazır kullanıcılarla gelir:

| Rol | E-posta | Şifre |
|-----|---------|-------|
| Yönetici (Admin) | admin@sifa.com | admin123 |
| Kayıt Görevlisi | ayse@sifa.com | kayit123 |
| Randevu Görevlisi | fatma@sifa.com | randevu123 |
| Veznedar | mehmet@sifa.com | vezne123 |
| Doktor | ahmet.dr@sifa.com | doktor123 |

---

## Roller ve Yetkiler

| Rol | Yapabilecekleri |
|-----|----------------|
| **Yönetici** | Klinik/doktor CRUD, tüm muayene ve ödemeleri görme, randevu görüntüleme, rol atama, görevli ekleme |
| **Kayıt Görevlisi** | Hasta sorgulama, ekleme, silme |
| **Randevu Görevlisi** | Randevu oluşturma ve düzenleme (hasta arama, klinik → doktor seçimi) |
| **Veznedar** | Ödeme işlemleri |
| **Doktor** | Kendi dashboard'u, atanmış randevuları, hastaların muayene geçmişi |

---

## API Dokümantasyonu

Backend çalışırken Swagger UI'ya şu adresten ulaşılabilir:

```
http://localhost:8080/swagger-ui/index.html
```

H2 veritabanı konsoluna (sadece geliştirme modunda):

```
http://localhost:8080/h2-console
```
> JDBC URL: `jdbc:h2:file:./data/sifa_poliklinik_db` · Kullanıcı: `sa` · Şifre: _(boş)_

---

## Proje Yapısı

```
SifaPoliklinik/
├── backend/
│   ├── src/
│   │   └── main/java/com/sifa/poliklinik/
│   │       ├── config/       # Security, CORS, JWT, seed data
│   │       ├── controller/   # REST API endpoint'leri
│   │       ├── dto/          # İstek/yanıt modelleri
│   │       ├── entity/       # JPA entity'leri
│   │       ├── repository/   # Veritabanı erişim katmanı
│   │       └── service/      # İş mantığı
│   ├── data/                 # H2 veritabanı dosyaları (hazır gelir)
│   └── pom.xml
└── frontend/
    ├── src/
    │   ├── components/       # Tekrar kullanılabilir UI bileşenleri
    │   ├── pages/            # Sayfa bileşenleri (rol bazlı)
    │   └── services/         # API çağrıları (axios)
    ├── package.json
    └── vite.config.js
```

---

## Sık Karşılaşılan Sorunlar

**`JAVA_HOME` hatası alıyorum**  
Java kurulumunun PATH'e eklendiğinden emin ol: `java -version` çalışmalı.

**Port zaten kullanımda**  
Backend için 8080, frontend için 5173 portlarının boş olduğunu kontrol et.

**`npm install` sonrası frontend başlamıyor**  
Node.js sürümünün 18+ olduğunu doğrula: `node -v`
