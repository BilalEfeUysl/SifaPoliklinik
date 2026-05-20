# Şifa Poliklinik - Risk Yönetimi ve Risk Tablosu

Bu doküman, Şifa Poliklinik yazılım projesinin geliştirme ve işletme aşamalarında karşılaşılabilecek riskleri analiz eder ve ele alınacak riskler için risk bilgi sayfalarını sunar.

## 1. Risk Tablosu

Aşağıdaki tabloda projenin proje, teknik ve işletme riskleri değerlendirilmiştir. Düşük olasılık ve düşük etkili riskler elenmiş, yalnızca ele alınması gereken temel riskler listelenmiştir.

| ID | Risk Adı | Risk Çeşidi | Olasılık | Etki |
| :--- | :--- | :--- | :--- | :--- |
| **RSK-01** | Gereksinimlerin Yanlış Anlaşılması | Proje Riski | Orta | Yüksek  |
| **RSK-02** | Projenin Zamanında Yetişmemesi | Proje Riski | Yüksek | Orta |
| **RSK-03** | Yeni Teknolojilerde Deneyimsizlik | Teknik Risk | Orta | Orta |
| **RSK-04** | Veritabanı ve Veri Kaybı Bug'ları | Teknik Risk | Düşük | Çok Yüksek |
| **RSK-05** | Entegrasyon Hataları | Teknik Risk | Orta | Yüksek |
| **RSK-06** | Sistemin Personel Tarafından Kullanılamaması | İşletme Riski | Orta | Yüksek |
| **RSK-07** | Randevu Çakışmaları ve Hastane Düzeninin Bozulması | İşletme Riski | Yüksek | Çok Yüksek |
| **RSK-08** | Log Verilerinin Kalıcı Olmaması (Audit Log) | Teknik Risk | Kesin | Yüksek |

---

## 2. Risk Bilgi Sayfaları

Aşağıda, tablodaki kritik risklerin her biri için detaylı bilgiler, önleme yolları ve alternatif planlar sunulmuştur.

### RSK-01: Gereksinimlerin Yanlış Anlaşılması
*   **Çeşit:** Proje Riski
*   **Bilgiler:** Müşterinin tam olarak ne istediğinin analiz aşamasında eksik veya yanlış anlaşılması. Örneğin muayene periyotlarının 30 dakika yerine farklı düşünülmesi.
*   **Önleme Yolları:** Sık aralıklarla müşteri toplantıları yapmak, "İzlenebilirlik Tablosu" kullanarak gereksinimleri kodla eşleştirmek, prototipler üzerinden onay almak.
*   **Alternatif Plan :** Yazılımın modüler yapıda geliştirilmiş olması sayesinde, yanlış anlaşılan modülün hızlıca refactor edilmesi. Gerekirse canlıya çıkış tarihinin revize edilmesi.

### RSK-02: Projenin Zamanında Yetişmemesi
*   **Çeşit:** Proje Riski
*   **Bilgiler:** Projenin öngörülen takvimden saparak geliştirme süresinin uzaması ve maliyetlerin artmasına neden olması.
*   **Önleme Yolları:** Çevik proje yönetimi kullanmak. İşleri küçük ve ölçülebilir task'lara bölmek .
*   **Alternatif Plan:** "Olsa iyi olur"  özelliklerin  ikinci faza ertelenerek uygulamanın temel işlevlerle zamanında teslim edilmesi.

### RSK-03: Yeni Teknolojilerde Deneyimsizlik
*   **Çeşit:** Teknik Risk
*   **Bilgiler:** Ekipteki yazılımcıların kullanılan güncel versiyonlara  veya kütüphanelere tam hakim olmaması nedeniyle kalite düşüklüğü ve bug'ların artması.
*   **Önleme Yolları:** Kodlamaya geçmeden önce araştırma yapılması. Sık sık takım içi kod incelemeleri yapılması.
*   **Alternatif Plan:** Takıma deneyimli bir danışman dahil etmek veya ekibin daha iyi bildiği/hakim olduğu daha stabil kütüphanelere geri dönüş yapılması.

### RSK-04: Veritabanı ve Veri Kaybı Bug'ları
*   **Çeşit:** Teknik Risk
*   **Bilgiler:** Transaction yönetiminin yanlış kurgulanması sonucu hasta, randevu veya ödeme verilerinin kaybolması.
*   **Önleme Yolları:** Kritik işlemlerde  `@Transactional` anotasyonunun zorunlu tutulması. Kapsamlı test senaryolarının işletilmesi.
*   **Alternatif Plan:** Sunucu tarafında periyodik veritabanı yedeklemesi  almak ve sistem çöktüğünde en yakın yedeğe geri dönebilmek.

### RSK-05: Entegrasyon Hataları
*   **Çeşit:** Teknik Risk
*   **Bilgiler:** Dış sistemlerle haberleşirken servislerin çökmesi, zaman aşımına uğraması veya teknolojinin değişmesi.
*   **Önleme Yolları:** Dış çağrıları Hata Yakalama  blokları içine almak.
*   **Alternatif Plan:** Servis çöktüğünde sistemin kilitlenmesini önlemek için, geçici olarak "Sahte Başarı" dönen veya doğrulamanın manuel yapılmasını sağlayan bir bypass moduna geçilmesi.

### RSK-06: Sistemin Personel Tarafından Kullanılamaması
*   **Çeşit:** İşletme  Riski
*   **Bilgiler:** Arayüzün karmaşık olması veya personelin uygulamayı öğrenememesi sebebiyle sisteme direnç göstermesi .
*   **Önleme Yolları:** Kullanıcı arayüzünde  modern, temiz ve anlaşılır bir tasarım dilinin tercih edilmesi. Kullanıcılara işlem sırasında yönlendirmeler sunulması.
*   **Alternatif Plan:** Devreye alım öncesinde personellere eğitim toplantıları düzenlemek ve bir kullanım kılavuzu oluşturmak.

### RSK-07: Randevu Çakışmaları ve Hastane Düzeninin Bozulması
*   **Çeşit:** İşletme Riski
*   **Bilgiler:** Yazılımsal bir eksiklik yüzünden aynı doktora aynı saatte birden fazla hastanın atanması sonucu poliklinikte büyük aksaklık  yaşanması.
*   **Önleme Yolları:** Backend'de randevu sistemi için katı iş kuralları uygulanması. Çakışma kontrolü için veritabanı sorgularının sıkılaştırılması.
*   **Alternatif Plan:** Bir hata sonucu çakışma oluşursa, sistemin yöneticiye acil log/uyarı mesajı üretmesi; en son alınan randevunun otomatik "İptal" edilerek hastaya yeniden planlama için ulaşılması.

### RSK-08: Log Verilerinin Kalıcı Olmaması
*   **Çeşit:** Teknik Risk
*   **Bilgiler:** Projenin H2 veritabanı ile çalışması nedeniyle, sistem yöneticisi veya yazılımcı sunucuyu yeniden başlattığında tutulan tüm işlem geçmişi verilerinin tamamen silinmesi.
*   **Önleme Yolları:** Audit Log verilerini periyodik olarak fiziksel bir dosyaya yazmak veya H2 yapılandırmasını dosya tabanlı  çalışacak şekilde ayarlamak.
*   **Alternatif Plan:** Üretime  çıkılmadan önce veritabanı altyapısının acilen PostgreSQL veya MySQL gibi kalıcı bir RDBMS'e taşınması.
