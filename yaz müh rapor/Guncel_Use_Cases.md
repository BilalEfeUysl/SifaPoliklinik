# Kullanım Senaryosu Metinleri

## UC-01: Yeni Hasta Kaydı Oluşturma
**Aktör:** Kayıt Görevlisi
**Ön Koşullar:** Sisteme `ROLE_KAYIT_GOREVLISI` yetkisiyle giriş yapılmış olmalıdır. TC Kimlik No sistemde daha önceden kayıtlı olmamalıdır.

**Ana Başarı Senaryosu:**
1. Hasta, kimlik ve iletişim bilgilerini iletir.
2. Görevli, sistemden "Yeni Hasta Kaydı" formunu açar; TC Kimlik No, Ad, Soyad, Doğum Tarihi, Telefon, Adres ve SGK Durumu bilgilerini girer.
3. Sistem, TC Kimlik numarasının benzersizliğini doğrular.
4. Sistem, kaydı oluşturur, hastayı veritabanına ekler ve başarıyla kaydedildiğini gösterir.

**Alternatif Senaryolar:**
* **3a. Hasta Zaten Kayıtlıysa:** Sistem "Bu TC Kimlik numarası ile kayıtlı hasta zaten mevcut" şeklinde bir uyarı fırlatır ve kayıt işlemini iptal eder.

---

## UC-02: Randevu Oluşturma
**Aktör:** Randevu Görevlisi
**Ön Koşullar:** Sisteme `ROLE_RANDEVU_GOREVLISI` yetkisiyle giriş yapılmış olmalıdır. Hastanın geçmişten kalan ödenmemiş borcu bulunmamalıdır. Seçilen doktor randevu kabulüne uygun olmalıdır.

**Ana Başarı Senaryosu:**
1. Hasta, muayene olmak istediği klinik/doktor ve tarih tercihini iletir.
2. Görevli, sistemi sorgular ve mesai saatleri içinde uygun 30 dakikalık randevu dilimlerini listeler.
3. Uygun saat seçilir ve randevuyu alacak olan hastanın bilgileri seçilir/girilir.
4. Sistem, hastanın geçmiş borç durumunu kontrol eder.
5. Sistem randevuyu `BEKLIYOR` statüsünde kaydeder ve özeti ekranda gösterir.

**Alternatif Senaryolar:**
* **2a. Boş Zaman Yoksa veya Doktor Müsait Değilse:** Sistem o doktor için uygun saat olmadığını veya doktorun randevu kabul etmediğini belirtir, alternatif tarih/doktor seçimi gerekir.
* **4a. Borç Kontrolü Başarısız:** Sistem "RANDEVU REDDEDİLDİ: Hasta borçludur" uyarısı verir ve randevu kaydetme işlemini bloke eder.

---

## UC-03: Muayene Gerçekleştirme
**Aktör:** Doktor
**Ön Koşullar:** Sisteme `ROLE_DOKTOR` yetkisiyle giriş yapılmış olmalıdır. İlgili saate ait `BEKLIYOR` statüsünde bir randevu bulunmalıdır.

**Ana Başarı Senaryosu:**
1. Doktor, günlük randevu listesinden sırası gelen  randevuyu seçer.
2. Sistem, randevu detaylarını ve hastayı ekrana getirir.
3. Doktor, muayene bulgularını sisteme metin olarak girer.
4. Sistem, randevunun statüsünü `TAMAMLANDI` olarak günceller.
5. Sistem **arka planda**, muayene ücreti üzerinden hastanın SGK indirimini hesaplayarak `BEKLIYOR` statüsünde yeni bir **Ödeme** kaydı oluşturur.

**Genişletme Senaryoları:**
* **Reçete Yazma:** Doktor sisteme ilaç ve dozaj bilgilerini girer, reçete doğrudan muayene kaydına bağlanır.
* **Rapor/Sevk Verme:** Doktor istirahat raporu veya kurum sevk bilgisini metin olarak muayene kaydına ekler.
* **Geçmiş Muayeneleri Görüntüleme:** Doktor, "Doktor Dashboard" ekranı üzerinden hastanın eski muayenelerini kronolojik olarak listeler ve tüm detaylarını inceler.

---

## UC-04: Ücret Hesaplama ve Tahsilat
**Ana Aktör:** Veznedar
**Yardımcı Aktör:** SGK Sistemi 
**Ön Koşullar:** Sisteme `ROLE_VEZNEDAR` yetkisiyle giriş yapılmış olmalıdır. Hastanın `BEKLIYOR` statüsünde bir ödeme kaydı bulunmalıdır.

**Ana Başarı Senaryosu:**
1. Veznedar, bekleyen ödemeler listesinden hastanın ilgili ödeme kaydını açar veya hastanın TC/Ad-Soyad bilgisi ile sistemi sorgular.
2. Sistem, muayene sırasında SGK Adapter ile hesaplanmış olan Toplam Tutarı, SGK İndirim Tutarını ve hastadan tahsil edilecek Net Tutarı ekranda gösterir.
3. Veznedar ödemeyi alır ve sistem üzerinden işlemi onaylar.
4. Sistem, ödemenin statüsünü `ODENDI` yapar ve ödeme tarihini/saatini güncelleyerek işlemi tamamlar.

**Alternatif Senaryolar:**
* **2a. SGK İndirimi Yoksa :** SGK Adapter indirim oranını %0 olarak dönmüşse, indirim tutarı 0 olur ve Veznedar taban ücretin tamamını  tahsil eder.

---

## UC-05: Klinik Yönetimi
**Aktör:** Sistem Yöneticisi
**Ön Koşullar:** Sisteme `ROLE_YONETICI` yetkisiyle giriş yapılmış olmalıdır.

**Ana Başarı Senaryosu (Klinik Ekleme/Güncelleme):**
1. Yönetici, Klinik Yönetimi ekranını açar ve "Yeni Klinik Ekle" veya mevcut bir klinik için "Düzenle" seçeneğine tıklar.
2. Sistem, klinik bilgilerinin girileceği formu gösterir.
3. Yönetici; Klinik Adı, Açıklama ve o kliniğe özel "Muayene Ücreti" bilgilerini girer.
4. Sistem, klinik bilgilerini kaydeder ve başarı mesajı gösterir.

**Alternatif Senaryolar (Klinik Silme):**
* **Klinik Silme Kontrolü Başarısız:** Yönetici bir kliniği silmek istediğinde, eğer o kliniğe atanmış aktif bir doktor varsa, sistem "Klinikte kayıtlı doktor var, önce doktorları silin veya taşıyın." şeklinde `BusinessRuleException` hatası fırlatır ve silme işlemini durdurur.

---

## UC-06: Doktor Yönetimi
**Aktör:** Sistem Yöneticisi
**Ön Koşullar:** Sisteme `ROLE_YONETICI` yetkisiyle giriş yapılmış olmalıdır. Aktif durumda en az bir klinik bulunmalıdır.

**Ana Başarı Senaryosu (Yeni Doktor Ekleme):**
1. Yönetici, Doktor Yönetimi sayfasından "Yeni Doktor" butonuna tıklar.
2. Yönetici; doktorun kişisel bilgilerini , sisteme giriş yapacağı parolayı ve mesleki bilgilerini girer.
3. Sistem, öncelikle bu bilgilere sahip `ROLE_DOKTOR` yetkisine sahip bir Kullanıcı profili oluşturur.
4. Ardından bu kullanıcı profilini seçilen klinik ile ilişkilendirerek Doktor kaydını tamamlar.

**Genişletme  Senaryoları:**
* **Müsaitlik Durumu Değiştirme:** Yönetici, doktorların "Müsait / Müsait Değil" durumlarını (`musaitlikToggle`) doğrudan tek tuşla değiştirebilir. Müsait olmayan doktorlara randevu verilemez.
* **Şifre Sıfırlama:** Yönetici, doktor düzenleme ekranından doktorun şifresini yeni bir şifre ile değiştirebilir.
* **Doktor Silme:** Doktor silindiğinde, ona bağlı olan sisteme giriş yaptığı "Kullanıcı" hesabı da otomatik olarak silinir.

---

## UC-07: Personel  Yönetimi
**Aktör:** Sistem Yöneticisi
**Ön Koşullar:** Sisteme `ROLE_YONETICI` yetkisiyle giriş yapılmış olmalıdır.

**Ana Başarı Senaryosu:**
1. Yönetici, Kullanıcı/Personel ekranını açar ve sisteme yeni bir görevli eklemek ister.
2. Yönetici personelin Ad, Soyad, Email, Şifre, Telefon bilgilerini girer.
3. Personelin sistemdeki rolünü seçer.
4. Sistem, e-postanın benzersiz olduğunu doğrular ve kullanıcıyı sisteme kaydeder.

**Alternatif Senaryolar:**
* **2a. Email Kullanımda İse:** Sistem "Bu email zaten kullanımda" hatası fırlatır (`ConflictException`).
* **3a. Yanlış Rol Seçimi:** Yönetici bu ekrandan `DOKTOR` rolünü atamaya çalışırsa, sistem "Doktor eklemek için /yonetici/doktorlar endpoint'ini kullanın." hatası verir ve işlemi engeller. Aynı durum DOKTOR rolündeki birini güncellemeye veya silmeye çalışırken de geçerlidir.

---

## UC-08: Sistem Parametreleri ve İstatistikler
**Aktör:** Sistem Yöneticisi
**Ön Koşullar:** Sisteme `ROLE_YONETICI` yetkisiyle giriş yapılmış olmalıdır.

* **Sistem Parametreleri:** Yönetici; randevu kabul başlangıç/bitiş saatlerini, öğle molası saatini  ve varsayılan muayene taban ücretini uygulama konfigürasyon dosyalarından yönetebilir.
* **Dashboard İstatistikleri:** Sistem yöneticisi ana ekranda o anki Toplam Klinik Sayısını, Toplam Doktor Sayısını ve Toplam Sistem Kullanıcısı Sayısını tek bir ekrandan anlık olarak görüntüleyebilir.

---

## UC-09: Sistem İşlem Geçmişinin Tutulması
**Aktör:** Sistem 
**Ön Koşullar:** Herhangi bir kullanıcının yetki gerektiren kritik bir kayıt (Hasta, Randevu, Ödeme, vb.) işlemi başlatmış olması.

**Ana Başarı Senaryosu :**
1. Kullanıcı; kayıt oluşturma, güncelleme veya silme işlemlerinden birini tetikler.
2. İşlem veritabanına başarıyla yansıdığında sistem asenkron olarak Audit Log servisine bir sinyal gönderir.
3. Audit Log servisi; işlemi yapan kullanıcının email adresini, etkilenen kaydın ID'sini, yapılan işlemi ve zaman damgasını log tablosuna kaydeder.
4. Log kaydı sisteme yansıtılarak arka plan görevi sonlandırılır.
