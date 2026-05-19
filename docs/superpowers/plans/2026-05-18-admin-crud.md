# Admin Panel CRUD Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Klinik ve doktor için silme+güncelleme, görevliler için tam CRUD ekle.

**Architecture:** Backend `YoneticiController`'a yeni endpoint'ler eklenir; frontend `YoneticiPage.jsx`'e edit/delete butonları ve yeni Görevliler bölümü eklenir; `axiosConfig.js`'e yeni API metodları eklenir.

**Tech Stack:** Java Spring Boot, Spring Data JPA, React + Vite, Axios

---

## Dosya Haritası

- Modify: `backend/src/main/java/com/sifa/poliklinik/controller/YoneticiController.java`
- Create: `backend/src/main/java/com/sifa/poliklinik/dto/DoktorGuncelleRequest.java`
- Modify: `frontend/src/api/axiosConfig.js`
- Modify: `frontend/src/pages/YoneticiPage.jsx`

---

### Task 1: Klinik silme backend endpoint'i

**Files:**
- Modify: `backend/src/main/java/com/sifa/poliklinik/controller/YoneticiController.java`

- [ ] **Step 1: `YoneticiController.java`'ya klinik silme endpoint'ini ekle**

`klinikGuncelle` metodunun hemen altına (`// ---- Doktor Yönetimi ----` satırından önce) ekle:

```java
@DeleteMapping("/klinikler/{id}")
public ResponseEntity<Void> klinikSil(@PathVariable Long id) {
    Klinik klinik = klinikRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Klinik bulunamadı: ID " + id));
    if (!klinik.getDoktorlar().isEmpty()) {
        throw new BusinessRuleException("Klinikte kayıtlı doktor var, önce doktorları silin veya taşıyın.");
    }
    klinikRepository.delete(klinik);
    return ResponseEntity.noContent().build();
}
```

- [ ] **Step 2: Backend'i derle**

```bash
cd /Users/bilal_efe/Desktop/yazmüh/backend && mvn compile -q
```
Beklenen çıktı: BUILD SUCCESS

---

### Task 2: Doktor güncelleme + silme backend endpoint'leri

**Files:**
- Create: `backend/src/main/java/com/sifa/poliklinik/dto/DoktorGuncelleRequest.java`
- Modify: `backend/src/main/java/com/sifa/poliklinik/controller/YoneticiController.java`
- Modify: `backend/src/main/java/com/sifa/poliklinik/repository/DoktorRepository.java`

- [ ] **Step 1: `DoktorGuncelleRequest.java` oluştur**

```java
package com.sifa.poliklinik.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoktorGuncelleRequest {
    @NotBlank(message = "Ünvan boş olamaz")
    private String unvan;

    @NotBlank(message = "Uzmanlık alanı boş olamaz")
    private String uzmanlikAlani;

    @NotNull(message = "Klinik seçilmelidir")
    private Long klinikId;

    private String sifre;
}
```

- [ ] **Step 2: `DoktorRepository.java`'ya doktor sayısı sorgusunu ekle**

```java
boolean existsByKullaniciId(Long kullaniciId);
```

Mevcut `@Repository` interface'ine diğer metodların yanına ekle.

- [ ] **Step 3: `YoneticiController.java`'ya gerekli importları ekle**

Dosyanın üstündeki import bloğuna şunları ekle (henüz yoksa):

```java
import com.sifa.poliklinik.dto.DoktorGuncelleRequest;
import com.sifa.poliklinik.exception.BusinessRuleException;
import org.springframework.transaction.annotation.Transactional;
```

- [ ] **Step 4: `YoneticiController.java`'ya doktor güncelleme endpoint'ini ekle**

`doktorMusaitlikToggle` metodunun hemen altına ekle:

```java
@PutMapping("/doktorlar/{id}")
public ResponseEntity<Doktor> doktorGuncelle(@PathVariable Long id,
        @Valid @RequestBody DoktorGuncelleRequest request) {
    Doktor doktor = doktorRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Doktor bulunamadı: ID " + id));
    Klinik klinik = klinikRepository.findById(request.getKlinikId())
            .orElseThrow(() -> new NotFoundException("Klinik bulunamadı"));
    doktor.setUnvan(request.getUnvan());
    doktor.setUzmanlikAlani(request.getUzmanlikAlani());
    doktor.setKlinik(klinik);
    if (request.getSifre() != null && !request.getSifre().isBlank()) {
        doktor.getKullanici().setSifre(passwordEncoder.encode(request.getSifre()));
        kullaniciRepository.save(doktor.getKullanici());
    }
    return ResponseEntity.ok(doktorRepository.save(doktor));
}
```

- [ ] **Step 5: `YoneticiController.java`'ya doktor silme endpoint'ini ekle**

Doktor güncelleme metodunun hemen altına ekle:

```java
@DeleteMapping("/doktorlar/{id}")
@Transactional
public ResponseEntity<Void> doktorSil(@PathVariable Long id) {
    Doktor doktor = doktorRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Doktor bulunamadı: ID " + id));
    Long kullaniciId = doktor.getKullanici().getId();
    doktorRepository.delete(doktor);
    kullaniciRepository.deleteById(kullaniciId);
    return ResponseEntity.noContent().build();
}
```

- [ ] **Step 6: Backend'i derle**

```bash
cd /Users/bilal_efe/Desktop/yazmüh/backend && mvn compile -q
```
Beklenen çıktı: BUILD SUCCESS

---

### Task 3: Görevli yönetimi backend endpoint'leri

**Files:**
- Modify: `backend/src/main/java/com/sifa/poliklinik/controller/YoneticiController.java`

`RegisterRequest` DTO'su zaten `ad, soyad, email, sifre, rol, telefon` alanlarına sahip — yeni DTO oluşturmaya gerek yok.

- [ ] **Step 1: `YoneticiController.java`'ya `RegisterRequest` importunu ekle**

```java
import com.sifa.poliklinik.dto.RegisterRequest;
```

- [ ] **Step 2: Görevli ekleme endpoint'ini ekle**

`// ---- Dashboard İstatistikleri ----` satırından önce ekle:

```java
// ---- Görevli Yönetimi ----

@PostMapping("/kullanicilar")
public ResponseEntity<Kullanici> gorevliEkle(@Valid @RequestBody RegisterRequest request) {
    if (request.getRol() == Rol.DOKTOR) {
        throw new BusinessRuleException("Doktor eklemek için /yonetici/doktorlar endpoint'ini kullanın.");
    }
    if (kullaniciRepository.findByEmail(request.getEmail()).isPresent()) {
        throw new ConflictException("Bu email zaten kullanımda: " + request.getEmail());
    }
    Kullanici kullanici = Kullanici.builder()
            .ad(request.getAd())
            .soyad(request.getSoyad())
            .email(request.getEmail())
            .sifre(passwordEncoder.encode(request.getSifre()))
            .rol(request.getRol())
            .telefon(request.getTelefon())
            .build();
    return ResponseEntity.ok(kullaniciRepository.save(kullanici));
}
```

- [ ] **Step 3: Görevli güncelleme endpoint'ini ekle**

`@Valid` kullanılmaz — `RegisterRequest.sifre` alanı `@NotBlank` içeriyor, güncelleme sırasında şifre opsiyonel olacak.

```java
@PutMapping("/kullanicilar/{id}")
public ResponseEntity<Kullanici> gorevliGuncelle(@PathVariable Long id,
        @RequestBody RegisterRequest request) {
    Kullanici kullanici = kullaniciRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı: ID " + id));
    if (kullanici.getRol() == Rol.DOKTOR) {
        throw new BusinessRuleException("Doktor bilgilerini güncellemek için /yonetici/doktorlar endpoint'ini kullanın.");
    }
    if (request.getRol() == Rol.DOKTOR) {
        throw new BusinessRuleException("Bu endpoint üzerinden DOKTOR rolü atanamaz.");
    }
    kullanici.setAd(request.getAd());
    kullanici.setSoyad(request.getSoyad());
    kullanici.setEmail(request.getEmail());
    kullanici.setTelefon(request.getTelefon());
    kullanici.setRol(request.getRol());
    if (request.getSifre() != null && !request.getSifre().isBlank()) {
        kullanici.setSifre(passwordEncoder.encode(request.getSifre()));
    }
    return ResponseEntity.ok(kullaniciRepository.save(kullanici));
}
```

- [ ] **Step 4: Görevli silme endpoint'ini ekle**

```java
@DeleteMapping("/kullanicilar/{id}")
public ResponseEntity<Void> gorevliSil(@PathVariable Long id) {
    Kullanici kullanici = kullaniciRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı: ID " + id));
    if (kullanici.getRol() == Rol.DOKTOR) {
        throw new BusinessRuleException("Doktor silmek için /yonetici/doktorlar endpoint'ini kullanın.");
    }
    kullaniciRepository.delete(kullanici);
    return ResponseEntity.noContent().build();
}
```

- [ ] **Step 5: `ConflictException` importunu ekle (yoksa)**

```java
import com.sifa.poliklinik.exception.ConflictException;
```

- [ ] **Step 6: `KullaniciRepository`'de `findByEmail` metodunun var olduğunu doğrula**

```bash
grep -n "findByEmail" /Users/bilal_efe/Desktop/yazmüh/backend/src/main/java/com/sifa/poliklinik/repository/KullaniciRepository.java
```

Eğer yoksa şu satırı ekle:
```java
Optional<Kullanici> findByEmail(String email);
```
(ve `import java.util.Optional;` ekle)

- [ ] **Step 7: Backend'i derle**

```bash
cd /Users/bilal_efe/Desktop/yazmüh/backend && mvn compile -q
```
Beklenen çıktı: BUILD SUCCESS

---

### Task 4: Frontend axiosConfig.js güncelleme

**Files:**
- Modify: `frontend/src/api/axiosConfig.js`

- [ ] **Step 1: `yoneticiAPI` objesini güncelle**

`axiosConfig.js` dosyasındaki `yoneticiAPI` bloğunu şununla değiştir:

```js
export const yoneticiAPI = {
  getStats: () => api.get('/yonetici/istatistikler'),
  getKlinikler: () => api.get('/yonetici/klinikler'),
  getDoktorlar: () => api.get('/yonetici/doktorlar'),
  getKullanicilar: () => api.get('/yonetici/kullanicilar'),
  addKlinik: (data) => api.post('/yonetici/klinikler', data),
  updateKlinik: (id, data) => api.put(`/yonetici/klinikler/${id}`, data),
  deleteKlinik: (id) => api.delete(`/yonetici/klinikler/${id}`),
  addDoktor: (data) => api.post('/yonetici/doktorlar', data),
  updateDoktor: (id, data) => api.put(`/yonetici/doktorlar/${id}`, data),
  deleteDoktor: (id) => api.delete(`/yonetici/doktorlar/${id}`),
  toggleDoktorMusaitlik: (id) => api.put(`/yonetici/doktorlar/${id}/musaitlik`),
  addGorevli: (data) => api.post('/yonetici/kullanicilar', data),
  updateGorevli: (id, data) => api.put(`/yonetici/kullanicilar/${id}`, data),
  deleteGorevli: (id) => api.delete(`/yonetici/kullanicilar/${id}`),
};
```

---

### Task 5: Frontend YoneticiPage.jsx — Klinik düzenleme ve silme

**Files:**
- Modify: `frontend/src/pages/YoneticiPage.jsx`

- [ ] **Step 1: Klinik state'lerini ve handler'larını ekle**

Mevcut `showKlinikModal` state'lerinin altına ekle:

```jsx
const [editKlinik, setEditKlinik] = useState(null); // null = ekleme modu, obje = düzenleme modu
```

- [ ] **Step 2: `handleKlinikSubmit` fonksiyonunu güncelle**

Mevcut `handleKlinikSubmit`'i şununla değiştir:

```jsx
const handleKlinikSubmit = async (e) => {
  e.preventDefault();
  try {
    if (editKlinik) {
      await yoneticiAPI.updateKlinik(editKlinik.id, {
        ...klinikForm,
        muayeneUcreti: parseFloat(klinikForm.muayeneUcreti)
      });
    } else {
      await yoneticiAPI.addKlinik({
        ...klinikForm,
        muayeneUcreti: parseFloat(klinikForm.muayeneUcreti)
      });
    }
    setShowKlinikModal(false);
    setEditKlinik(null);
    setKlinikForm({ ad: '', aciklama: '', muayeneUcreti: '' });
    loadData();
  } catch (err) {
    alert(err.response?.data?.mesaj || 'Klinik kaydedilemedi.');
  }
};
```

- [ ] **Step 3: Klinik silme handler'ını ekle**

```jsx
const handleKlinikSil = async (id) => {
  if (!window.confirm('Bu kliniği silmek istediğinizden emin misiniz?')) return;
  try {
    await yoneticiAPI.deleteKlinik(id);
    loadData();
  } catch (err) {
    alert(err.response?.data?.mesaj || 'Klinik silinemedi.');
  }
};
```

- [ ] **Step 4: Klinik tablosunu güncelle**

Mevcut klinik tablosu `<thead>` ve `<tbody>` kısmını şununla değiştir:

```jsx
<thead><tr>
  <th>Klinik Adı</th><th>Ücret</th><th>İşlem</th>
</tr></thead><tbody>
  {klinikler.map(k=>(
    <tr key={k.id}>
      <td style={{fontWeight:600}}>{k.ad}</td>
      <td>{k.muayeneUcreti} TL</td>
      <td style={{display:'flex',gap:4}}>
        <button className="btn btn-sm btn-secondary" style={{fontSize:11,padding:'4px 8px'}}
          onClick={() => {
            setEditKlinik(k);
            setKlinikForm({ ad: k.ad, aciklama: k.aciklama || '', muayeneUcreti: k.muayeneUcreti });
            setShowKlinikModal(true);
          }}>Düzenle</button>
        <button className="btn btn-sm btn-danger" style={{fontSize:11,padding:'4px 8px'}}
          onClick={() => handleKlinikSil(k.id)}>Sil</button>
      </td>
    </tr>
  ))}
</tbody>
```

- [ ] **Step 5: Klinik modal başlığını güncelle**

Modal içindeki `<h3>Yeni Klinik Ekle</h3>` kısmını şununla değiştir:

```jsx
<h3>{editKlinik ? 'Klinik Düzenle' : 'Yeni Klinik Ekle'}</h3>
```

- [ ] **Step 6: Klinik modal kapanma handler'larını güncelle**

Modal `onClick` ve `onSubmit`'ten sonra `setEditKlinik(null)` çağrısı gelsin. Klinik modal'da `setShowKlinikModal(false)` olan her yerde `setEditKlinik(null)` de ekle:

"Yeni" butonunun `onClick`'ini şununla değiştir:
```jsx
onClick={() => { setEditKlinik(null); setKlinikForm({ ad: '', aciklama: '', muayeneUcreti: '' }); setShowKlinikModal(true); }}
```

Modal overlay `onClick`'ini şununla değiştir:
```jsx
onClick={() => { setShowKlinikModal(false); setEditKlinik(null); }}
```

X butonunun `onClick`'ini şununla değiştir:
```jsx
onClick={() => { setShowKlinikModal(false); setEditKlinik(null); }}
```

---

### Task 6: Frontend YoneticiPage.jsx — Doktor düzenleme ve silme

**Files:**
- Modify: `frontend/src/pages/YoneticiPage.jsx`

- [ ] **Step 1: Doktor edit state'i ekle**

```jsx
const [editDoktor, setEditDoktor] = useState(null);
```

- [ ] **Step 2: `handleDoktorSubmit` fonksiyonunu güncelle**

```jsx
const handleDoktorSubmit = async (e) => {
  e.preventDefault();
  try {
    if (editDoktor) {
      await yoneticiAPI.updateDoktor(editDoktor.id, {
        unvan: doktorForm.unvan,
        uzmanlikAlani: doktorForm.uzmanlikAlani,
        klinikId: parseInt(doktorForm.klinikId),
        sifre: doktorForm.sifre || undefined
      });
    } else {
      await yoneticiAPI.addDoktor({
        ...doktorForm,
        klinikId: parseInt(doktorForm.klinikId)
      });
    }
    setShowDoktorModal(false);
    setEditDoktor(null);
    setDoktorForm({ ad: '', soyad: '', email: '', sifre: '', telefon: '', unvan: '', uzmanlikAlani: '', klinikId: '' });
    loadData();
  } catch (err) {
    alert(err.response?.data?.mesaj || 'Doktor kaydedilemedi.');
  }
};
```

- [ ] **Step 3: Doktor silme handler'ını ekle**

```jsx
const handleDoktorSil = async (id) => {
  if (!window.confirm('Bu doktoru silmek istediğinizden emin misiniz? Bağlı tüm randevular da silinecektir.')) return;
  try {
    await yoneticiAPI.deleteDoktor(id);
    loadData();
  } catch (err) {
    alert(err.response?.data?.mesaj || 'Doktor silinemedi.');
  }
};
```

- [ ] **Step 4: Doktor tablosunu güncelle**

Mevcut doktor tablosu `<thead>` ve `<tbody>` kısmını şununla değiştir:

```jsx
<thead><tr>
  <th>Doktor</th><th>Klinik</th><th>Durum</th><th>İşlem</th>
</tr></thead><tbody>
  {doktorlar.map(d=>(
    <tr key={d.id}>
      <td style={{fontWeight:600}}>{d.unvan} {d.kullanici?.ad} {d.kullanici?.soyad}</td>
      <td>{d.klinik?.ad}</td>
      <td><span className={`badge ${d.musaitMi !== false ? 'badge-success' : 'badge-danger'}`}>
        {d.musaitMi !== false ? 'Müsait' : 'Müsait Değil'}
      </span></td>
      <td style={{display:'flex',gap:4,flexWrap:'wrap'}}>
        <button className="btn btn-sm btn-secondary" style={{fontSize:11,padding:'4px 8px'}}
          onClick={() => handleDoktorMusaitlikToggle(d.id)}>
          {d.musaitMi !== false ? 'Pasife Al' : 'Aktife Al'}
        </button>
        <button className="btn btn-sm btn-secondary" style={{fontSize:11,padding:'4px 8px'}}
          onClick={() => {
            setEditDoktor(d);
            setDoktorForm({ ad: '', soyad: '', email: '', sifre: '', telefon: '', unvan: d.unvan, uzmanlikAlani: d.uzmanlikAlani, klinikId: String(d.klinik?.id) });
            setShowDoktorModal(true);
          }}>Düzenle</button>
        <button className="btn btn-sm btn-danger" style={{fontSize:11,padding:'4px 8px'}}
          onClick={() => handleDoktorSil(d.id)}>Sil</button>
      </td>
    </tr>
  ))}
</tbody>
```

- [ ] **Step 5: Doktor modal başlığını ve şifre alanını güncelle**

Modal `<h3>` kısmını:
```jsx
<h3>{editDoktor ? 'Doktor Düzenle' : 'Yeni Doktor Ekle'}</h3>
```

Şifre alanındaki `required` niteliğini kaldır (güncelleme sırasında zorunlu olmayacak) ve etiketi güncelle:
```jsx
<div className="form-group"><label className="form-label">{editDoktor ? 'Yeni Şifre (boş bırakılabilir)' : 'Şifre *'}</label>
  <input className="form-input" type="password" value={doktorForm.sifre} onChange={e=>setDoktorForm({...doktorForm, sifre:e.target.value})} {...(!editDoktor && {required:true})}/></div>
```

Düzenleme modunda ad/soyad/email alanlarını gizle (bunlar doktor güncelleme isteğinde gönderilmez):
```jsx
{!editDoktor && <>
  <div className="form-group"><label className="form-label">Ad *</label>
    <input className="form-input" value={doktorForm.ad} onChange={e=>setDoktorForm({...doktorForm, ad:e.target.value})} required/></div>
  <div className="form-group"><label className="form-label">Soyad *</label>
    <input className="form-input" value={doktorForm.soyad} onChange={e=>setDoktorForm({...doktorForm, soyad:e.target.value})} required/></div>
  <div className="form-group"><label className="form-label">Email (Giriş için) *</label>
    <input className="form-input" type="email" value={doktorForm.email} onChange={e=>setDoktorForm({...doktorForm, email:e.target.value})} required/></div>
</>}
```

- [ ] **Step 6: Doktor modal kapanma handler'larını güncelle**

"Yeni" butonunun `onClick`'ini:
```jsx
onClick={() => { setEditDoktor(null); setDoktorForm({ ad: '', soyad: '', email: '', sifre: '', telefon: '', unvan: '', uzmanlikAlani: '', klinikId: '' }); setShowDoktorModal(true); }}
```

Modal overlay ve X butonu `onClick`'lerini:
```jsx
onClick={() => { setShowDoktorModal(false); setEditDoktor(null); }}
```

---

### Task 7: Frontend YoneticiPage.jsx — Görevliler bölümü

**Files:**
- Modify: `frontend/src/pages/YoneticiPage.jsx`

- [ ] **Step 1: Görevliler state'lerini ekle**

```jsx
const [kullanicilar, setKullanicilar] = useState([]);
const [showGorevliModal, setShowGorevliModal] = useState(false);
const [editGorevli, setEditGorevli] = useState(null);
const [gorevliForm, setGorevliForm] = useState({ ad: '', soyad: '', email: '', sifre: '', telefon: '', rol: 'KAYIT_GOREVLISI' });
```

- [ ] **Step 2: `loadData` fonksiyonunu güncelle**

`yoneticiAPI.getKullanicilar()` çağrısını zaten içeriyor ancak sonucu state'e atanmıyor. `loadData`'yı şununla değiştir:

```jsx
const loadData = async () => {
  try {
    const [s, k, d, u] = await Promise.all([
      yoneticiAPI.getStats().catch(() => ({ data: {} })),
      yoneticiAPI.getKlinikler().catch(() => ({ data: [] })),
      yoneticiAPI.getDoktorlar().catch(() => ({ data: [] })),
      yoneticiAPI.getKullanicilar().catch(() => ({ data: [] }))
    ]);
    setStats(s.data);
    setKlinikler(k.data);
    setDoktorlar(d.data);
    setKullanicilar(u.data.filter(u => u.rol !== 'DOKTOR'));
  } catch (err) { console.error(err); }
  finally { setLoading(false); }
};
```

- [ ] **Step 3: Görevli submit handler'ını ekle**

```jsx
const handleGorevliSubmit = async (e) => {
  e.preventDefault();
  try {
    if (editGorevli) {
      await yoneticiAPI.updateGorevli(editGorevli.id, {
        ...gorevliForm,
        sifre: gorevliForm.sifre || undefined
      });
    } else {
      await yoneticiAPI.addGorevli(gorevliForm);
    }
    setShowGorevliModal(false);
    setEditGorevli(null);
    setGorevliForm({ ad: '', soyad: '', email: '', sifre: '', telefon: '', rol: 'KAYIT_GOREVLISI' });
    loadData();
  } catch (err) {
    alert(err.response?.data?.mesaj || 'Görevli kaydedilemedi.');
  }
};
```

- [ ] **Step 4: Görevli silme handler'ını ekle**

```jsx
const handleGorevliSil = async (id) => {
  if (!window.confirm('Bu görevliyi silmek istediğinizden emin misiniz?')) return;
  try {
    await yoneticiAPI.deleteGorevli(id);
    loadData();
  } catch (err) {
    alert(err.response?.data?.mesaj || 'Görevli silinemedi.');
  }
};
```

- [ ] **Step 5: `ROL_ETIKETLERI` yardımcı sabitini ekle**

Bileşen fonksiyonunun dışında (en üste, import'ların altına) ekle:

```jsx
const ROL_ETIKETLERI = {
  KAYIT_GOREVLISI: 'Kayıt Görevlisi',
  RANDEVU_GOREVLISI: 'Randevu Görevlisi',
  VEZNEDAR: 'Veznedar',
  YONETICI: 'Yönetici',
};
```

- [ ] **Step 6: Görevliler bölümünü ve modalını JSX'e ekle**

Mevcut `{/* Doktor Ekleme Modal */}` bloğunun hemen öncesine, yani return'ün içindeki son kapanış `</div>`'ından önce Görevliler kartını ekle:

```jsx
{/* Görevliler */}
<div className="card" style={{ marginTop: 24 }}>
  <div className="card-header">
    <h3>Görevliler</h3>
    <button className="btn btn-sm btn-primary" onClick={() => { setEditGorevli(null); setGorevliForm({ ad: '', soyad: '', email: '', sifre: '', telefon: '', rol: 'KAYIT_GOREVLISI' }); setShowGorevliModal(true); }}>
      <Plus size={14} /> Yeni
    </button>
  </div>
  <div className="card-body" style={{ padding: 0 }}>
    <div className="data-table-wrapper"><table className="data-table"><thead><tr>
      <th>Ad Soyad</th><th>Rol</th><th>Email</th><th>Telefon</th><th>İşlem</th>
    </tr></thead><tbody>
      {kullanicilar.map(u=>(
        <tr key={u.id}>
          <td style={{fontWeight:600}}>{u.ad} {u.soyad}</td>
          <td><span className="badge badge-info">{ROL_ETIKETLERI[u.rol] || u.rol}</span></td>
          <td>{u.email}</td>
          <td>{u.telefon || '-'}</td>
          <td style={{display:'flex',gap:4}}>
            <button className="btn btn-sm btn-secondary" style={{fontSize:11,padding:'4px 8px'}}
              onClick={() => {
                setEditGorevli(u);
                setGorevliForm({ ad: u.ad, soyad: u.soyad, email: u.email, sifre: '', telefon: u.telefon || '', rol: u.rol });
                setShowGorevliModal(true);
              }}>Düzenle</button>
            <button className="btn btn-sm btn-danger" style={{fontSize:11,padding:'4px 8px'}}
              onClick={() => handleGorevliSil(u.id)}>Sil</button>
          </td>
        </tr>
      ))}
    </tbody></table></div>
  </div>
</div>
```

- [ ] **Step 7: Görevli modalını ekle**

Doktor ekleme modalının hemen altına ekle:

```jsx
{/* Görevli Ekleme/Düzenleme Modal */}
{showGorevliModal && (
  <div className="modal-overlay" onClick={() => { setShowGorevliModal(false); setEditGorevli(null); }}>
    <div className="modal" onClick={e=>e.stopPropagation()}>
      <div className="modal-header">
        <h3>{editGorevli ? 'Görevli Düzenle' : 'Yeni Görevli Ekle'}</h3>
        <button className="btn-icon" onClick={() => { setShowGorevliModal(false); setEditGorevli(null); }}><X size={18}/></button>
      </div>
      <form onSubmit={handleGorevliSubmit}>
        <div className="modal-body">
          <div className="form-grid">
            <div className="form-group"><label className="form-label">Ad *</label>
              <input className="form-input" value={gorevliForm.ad} onChange={e=>setGorevliForm({...gorevliForm, ad:e.target.value})} required/></div>
            <div className="form-group"><label className="form-label">Soyad *</label>
              <input className="form-input" value={gorevliForm.soyad} onChange={e=>setGorevliForm({...gorevliForm, soyad:e.target.value})} required/></div>
            <div className="form-group"><label className="form-label">Email *</label>
              <input className="form-input" type="email" value={gorevliForm.email} onChange={e=>setGorevliForm({...gorevliForm, email:e.target.value})} required/></div>
            <div className="form-group"><label className="form-label">{editGorevli ? 'Yeni Şifre (boş bırakılabilir)' : 'Şifre *'}</label>
              <input className="form-input" type="password" value={gorevliForm.sifre} onChange={e=>setGorevliForm({...gorevliForm, sifre:e.target.value})} {...(!editGorevli && {required:true})}/></div>
            <div className="form-group"><label className="form-label">Telefon</label>
              <input className="form-input" value={gorevliForm.telefon} onChange={e=>setGorevliForm({...gorevliForm, telefon:e.target.value})}/></div>
            <div className="form-group"><label className="form-label">Rol *</label>
              <select className="form-select" value={gorevliForm.rol} onChange={e=>setGorevliForm({...gorevliForm, rol:e.target.value})} required>
                <option value="KAYIT_GOREVLISI">Kayıt Görevlisi</option>
                <option value="RANDEVU_GOREVLISI">Randevu Görevlisi</option>
                <option value="VEZNEDAR">Veznedar</option>
                <option value="YONETICI">Yönetici</option>
              </select></div>
          </div>
        </div>
        <div className="modal-footer">
          <button type="button" className="btn btn-secondary" onClick={() => { setShowGorevliModal(false); setEditGorevli(null); }}>İptal</button>
          <button type="submit" className="btn btn-primary">Kaydet</button>
        </div>
      </form>
    </div>
  </div>
)}
```

- [ ] **Step 8: `badge-info` ve `btn-danger` CSS sınıflarının varlığını kontrol et**

```bash
grep -rn "badge-info\|btn-danger" /Users/bilal_efe/Desktop/yazmüh/frontend/src/
```

Eğer yoksa, mevcut CSS dosyasına (genellikle `index.css` veya `App.css`) ekle:

```css
.badge-info { background-color: #e0f0ff; color: #0066cc; }
.btn-danger { background-color: #dc2626; color: #fff; border: none; cursor: pointer; border-radius: 6px; }
.btn-danger:hover { background-color: #b91c1c; }
```

---

### Task 8: Son derleme ve test

- [ ] **Step 1: Backend'i tam derle**

```bash
cd /Users/bilal_efe/Desktop/yazmüh/backend && mvn compile -q
```
Beklenen çıktı: BUILD SUCCESS

- [ ] **Step 2: Frontend'i derle**

```bash
cd /Users/bilal_efe/Desktop/yazmüh/frontend && npm run build 2>&1 | tail -20
```
Beklenen çıktı: sıfır hata

- [ ] **Step 3: Backend'i başlat**

```bash
cd /Users/bilal_efe/Desktop/yazmüh/backend && mvn spring-boot:run &
```

- [ ] **Step 4: Frontend'i başlat**

```bash
cd /Users/bilal_efe/Desktop/yazmüh/frontend && npm run dev &
```

- [ ] **Step 5: Manuel test**

Tarayıcıda `http://localhost:5173` adresine git, admin hesabıyla giriş yap ve şunları doğrula:
1. Klinik listesinde "Düzenle" ve "Sil" butonları görünüyor
2. Klinik düzenleme modalı açılıyor ve değişiklikler kaydediliyor
3. Kliniği silmeye çalışırken doktor varsa hata mesajı geliyor
4. Doktor listesinde "Düzenle" ve "Sil" butonları görünüyor
5. Doktor güncelleme modalında ad/soyad/email alanları gizleniyor
6. "Görevliler" bölümü sayfada görünüyor, yeni görevli eklenebiliyor, düzenlenebiliyor ve silinebiliyor
