# Şifa Poliklinik - Kabuller ve Kısıtlar Belgesi

> Bu belge, Şifa Poliklinik Bilgi Sistemi'nin geliştirilmesinde esas alınan kabuller ile sistemin tabi olduğu kısıtları tanımlar. Gereksinim mühendisliği ve proje yönetimi süreçlerinde referans belgesi olarak kullanılır.

---

## BÖLÜM 1 — KABULLER 

> Kabuller: Proje boyunca doğru kabul edilen, ancak kesin olarak doğrulanmamış varsayımlardır. Gerçekleşmezlerse risk haline dönüşürler.

### 1.1 İş Ortamı Kabulleri

| ID | Kabul | Doğrulanmadığı Takdirde Risk |
|:---|:---|:---|
| **K-01** | Poliklinikte çalışma saatlerinin 09:00 – 17:00 olduğu ve değişmeyeceği kabul edilmiştir. | Randevu saati iş kuralları güncellenmek zorunda kalır. |
| **K-02** | Her muayene seansının tam 30 dakika sürdüğü ve randevuların yalnızca tam saat ve yarım saatte başladığı kabul edilmiştir. | Esnek süreli randevu sistemi eklemek gerekebilir. |
| **K-03** | Her gün 12:00 – 13:00 saatleri arasının öğle molası olduğu ve bu saatler için randevu verilemeyeceği kabul edilmiştir. | Öğle saatlerinde randevu talebi karşılanamaz. |
| **K-04** | Her doktorun yalnızca bir kliniğe bağlı olduğu kabul edilmiştir. Bir doktor birden fazla klinikte çalışmaz. | Doktor-klinik ilişkisi yeniden modellenmek zorunda kalır. |
| **K-05** | Hastanın ödenmemiş borcu varsa yeni randevu alması sistem tarafından otomatik engellenir; kasiyerin manuel müdahalesi gerekmez. | Borç istisnası yönetimi için ek iş akışı tasarlanmalıdır. |
| **K-06** | Poliklinikteki tüm muayenelerin sabit bir ücreti olduğu ve bu ücretin klinik bazında tanımlandığı kabul edilmiştir. | Esnek fiyatlandırma modülü eklenmelidir. |

### 1.2 Kullanıcı ve Personel Kabulleri

| ID | Kabul | Doğrulanmadığı Takdirde Risk |
|:---|:---|:---|
| **K-07** | Sistemi kullanan tüm personelin temel bilgisayar okuryazarlığına sahip olduğu kabul edilmiştir. | Kapsamlı bir eğitim ve destek süreci planlanmalıdır. |
| **K-08** | Her personelin yalnızca bir rolü bulunmaktadır. Aynı kişi hem kayıt görevlisi hem de veznedar olamaz. | Çoklu rol desteği için yetki mimarisi yeniden tasarlanmalıdır. |
| **K-09** | Sisteme erişim için internet bağlantısı ve modern bir web tarayıcısı mevcut olduğu kabul edilmiştir. | çevrimdışı çalışma modu desteklenmek zorunda kalınır. |
| **K-10** | Kullanıcıların sisteme yalnızca yönetici tarafından oluşturulan hesaplarla giriş yapabileceği kabul edilmiştir. Kendi hesabını açma yoktur. | Kullanıcı kayıt akışı ve e-posta doğrulama eklenmelidir. |
| **K-15** | Doktorların sisteme giriş yaptıklarında doğrudan "Doktor Dashboard" sayfasına yönlendirilip sadece kendi hastalarını görebileceği varsayılmıştır. | Doktorlar için yetki kısıtlamaları veya ayrı menüler geliştirilmelidir. |

### 1.3 Teknik Kabuller

| ID | Kabul | Doğrulanmadığı Takdirde Risk |
|:---|:---|:---|
| **K-11** | Geliştirme ve test ortamında H2 in-memory veritabanının yeterli olduğu, üretime geçişte PostgreSQL'e sorunsuz geçilebileceği kabul edilmiştir. | Veritabanı geçişi sırasında veri uyumsuzlukları oluşabilir. |
| **K-12** | JWT token'ının 24 saat geçerli olacağı ve bu sürenin güvenlik açısından kabul edilebilir olduğu varsayılmıştır. | Güvenlik politikası gereği token süresi kısaltılması veya refresh token mekanizması eklenmesi gerekebilir. |
| **K-13** | SGK entegrasyonunun geliştirme aşamasında Sahte Servis üzerinden yapılacağı ve gerçek SGK API'sine geçişin ileride SGKAdapter değiştirilerek sağlanacağı kabul edilmiştir. | Gerçek SGK API'si farklı bir veri formatı veya protokol kullanıyorsa adaptör yeniden yazılmalıdır. |
| **K-14** | TC Kimlik numarasının her zaman 11 haneli ve yalnızca rakamlardan oluştuğu kabul edilmiştir. | Uluslararası hasta kaydı için yabancı uyruklu kimlik desteği eklenmesi gerekir. |

---

## BÖLÜM 2 — KISITLAR 

> Kısıtlar: Sistemin tasarım, geliştirme veya işletme aşamalarında değiştirilemeyen sabit sınırlamalardır. İş kuralları, yasal zorunluluklar veya teknik seçimlerden kaynaklanırlar.

### 2.1 İş Kuralı Kısıtları 

| ID | Kısıt | Kaynak | Kodda Nerede? |
|:---|:---|:---|:---|
| **C-01** | Randevular yalnızca 09:00 – 16:30 arasında verilebilir. 17:00 ve sonrası kesinlikle yasaktır. | Poliklinik işletme kuralı | `RandevuService.randevuOlustur()` |
| **C-02** | Randevular yalnızca 00 veya 30 dakikada başlayabilir. | Muayene süresi standardı | `RandevuService.randevuOlustur()` |
| **C-03** | 12:00 – 13:00 saatleri arasına randevu verilemez . | Poliklinik işletme kuralı | `RandevuService.randevuOlustur()` |
| **C-04** | Aynı doktora aynı 30 dakikalık zaman dilimine birden fazla randevu verilemez. | Tıbbi hizmet kalitesi | `RandevuRepository.findCakisanRandevular()` |
| **C-05** | Borçlu hastaya yeni randevu verilemez. | Finansal iş kuralı | `HastaService.borcluMu()` |
| **C-06** | Sisteme giriş yapabilmek için kullanıcının geçerli e-posta ve şifreye sahip olması zorunludur. | Kimlik doğrulama gerekliliği | `AuthController`, `JwtAuthFilter` |
| **C-07** | Her kullanıcı yalnızca kendi rolünün izin verdiği işlemleri gerçekleştirebilir. | RBAC güvenlik kuralı | `SecurityConfig`, `@PreAuthorize` |

### 2.2 Teknik Kısıtlar 

| ID | Kısıt | Açıklama |
|:---|:---|:---|
| **C-08** | **Backend:** Java / Spring Boot 3.x | Proje başlangıcında seçilen teknoloji platformu; değişimi tüm backend kodunu etkiler. |
| **C-09** | **Frontend:** React 18 + Vite 5 | SPA  mimarisi; farklı bir framework'e geçiş tüm sayfaları yeniden yazmayı gerektirir. |
| **C-10** | **Veritabanı:** H2 In-Memory | Sunucu her yeniden başlatıldığında veriler sıfırlanır. Kalıcı depolama için PostgreSQL'e geçiş zorunludur. |
| **C-11** | **API Protokolü:** RESTful HTTP/JSON | Tüm frontend-backend haberleşmesi REST API üzerindendir; GraphQL veya gRPC gibi alternatifler mevcut mimariye uygun değildir. |
| **C-12** | **Kimlik Doğrulama:** JWT | Oturum  bazlı kimlik doğrulama kullanılmamaktadır; token yönetimi client tarafında yapılmaktadır. |
| **C-13** | **Port:** Backend 8080, Frontend 5173 | Geliştirme ortamında sabit port tanımları mevcuttur. Farklı port gereksinimlerinde CORS ve proxy ayarları güncellenmek zorundadır. |

### 2.3 Yasal ve Düzenleyici Kısıtlar 

| ID | Kısıt | Yasal Dayanak |
|:---|:---|:---|
| **C-14** | Hasta kişisel verileri KVKK kapsamında işlenmelidir. Yetkisiz erişim, paylaşım ve depolama yasaktır. | 6698 sayılı KVKK |
| **C-15** | Hastane bilgi sistemlerinin Sağlık Bakanlığı'nın belirlediği standartlara uygun olması beklenmektedir. | Sağlık Bakanlığı HBYS Yönetmeliği |
| **C-16** | TC Kimlik numarası, Sosyal Güvenlik Kurumu sorgulama işlemleri için zorunlu alan olarak kullanılmaktadır. Yabancı uyruklu hastalar için sistem henüz destek sağlamamaktadır. | SGK mevzuatı |

### 2.4 Kapsam Dışı Kısıtlar 

> Aşağıdaki özellikler mevcut sistem kapsamı dışındadır ve sistem bu işlevleri gerçeklemek zorunda değildir.

| ID | Kapsam Dışı Bırakılan Özellik | Gerekçe |
|:---|:---|:---|
| **D-01** | Hasta tarafı mobil uygulama veya web portalı | Yalnızca poliklinik personeline yönelik sistem |
| **D-02** | Online ödeme  entegrasyonu | Mevcut sistem yalnızca yüz yüze nakit/POS ödemelerini takip eder |
| **D-03** | E-Nabız / MHRS otomatik entegrasyonu | SGK entegrasyonu Mock servis düzeyinde; gerçek entegrasyon ileride planlanabilir |
| **D-04** | Çoklu şube / çoklu poliklinik desteği | Sistem tek bir poliklinik lokasyonu için tasarlanmıştır |
| **D-05** | Doktor vardiya ve izin yönetimi | Randevu sistemi çalışma saatlerini sabit kabul eder; vardiya yönetimi entegre değildir |
| **D-06** | İlaç stoğu ve eczane yönetimi | Sistemde yalnızca reçete metni yazılmaktadır; ilaç stoğu takibi kapsam dışıdır |
| **D-07** | Sistem İçi İşlem Kayıtlarının (Audit Log) UI üzerinden görüntülenmesi | Kayıtlar yalnızca veritabanı seviyesinde tutulur, bu aşamada admin ekranında gösterilmez |

---

## Özet Tablosu

| Kategori | Toplam Madde |
|:---|:---:|
| Kabuller | 15 |
| İş Kuralı Kısıtları | 7 |
| Teknik Kısıtlar | 6 |
| Yasal Kısıtlar | 3 |
| Kapsam Dışı Maddeler | 7 |
| **TOPLAM** | **38** |

---

*Not: Bu belgede yer alan kabuller ve kısıtlar, projenin 2026 Mayıs dönemi geliştirme aşamasını yansıtmaktadır. Sistemin kapsamı veya gereksinimleri değiştiğinde bu belge güncellenmeli ve tüm paydaşların onayına sunulmalıdır.*
