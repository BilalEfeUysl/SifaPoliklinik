# Sevk Kâğıdı Yazdır Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Muayene kaydında `sevkKurumu` dolu olduğunda "Sevk Kâğıdı Yazdır" butonuyla tarayıcının yazdırma diyaloğunu açan klasik resmi belge görünümü eklemek.

**Architecture:** Yalnızca frontend değişikliği. `MuayenePage.jsx` içine `SevkKagidiModal` bileşeni eklenir. Tarayıcı baskısı `window.print()` + `@media print` CSS ile yönetilir; yeni bağımlılık veya backend değişikliği yoktur.

**Tech Stack:** React (JSX), CSS @media print, lucide-react (Printer ikonu)

---

## Dosya Haritası

| Dosya | Değişiklik |
|-------|-----------|
| `frontend/src/index.css` | `@media print` kuralları eklenir (dosyanın sonuna) |
| `frontend/src/pages/MuayenePage.jsx` | `Printer` import eklenir; `showSevkModal` + `sevkMuayene` state; `SevkKagidiModal` bileşeni; iki tetikleyici nokta |

---

## Task 1: `@media print` CSS kuralları

**Files:**
- Modify: `frontend/src/index.css` (dosyanın sonuna ekle)

- [ ] **Adım 1: Print CSS ekle**

`frontend/src/index.css` dosyasının en sonuna ekle:

```css
/* --- Print (Sevk Kâğıdı) --- */
@media print {
  body > *:not(#sevk-print-root) { display: none !important; }
  #sevk-print-root { display: block !important; position: static !important; }
  #sevk-print-root .modal-overlay { position: static !important; background: none !important; }
  #sevk-print-root .sevk-modal-actions { display: none !important; }
}
```

- [ ] **Adım 2: Manuel kontrol**

Tarayıcıda başka bir sayfayı yazdırmaya çalış (Ctrl+P / Cmd+P) — mevcut hiçbir sayfa etkilenmemeli çünkü `#sevk-print-root` henüz DOM'da yok.

- [ ] **Adım 3: Commit**

```bash
git add frontend/src/index.css
git commit -m "style: add @media print rules for sevk kagidi"
```

---

## Task 2: `SevkKagidiModal` bileşeni + state

**Files:**
- Modify: `frontend/src/pages/MuayenePage.jsx`

- [ ] **Adım 1: `Printer` ikonunu import'a ekle**

`MuayenePage.jsx` satır 4'teki mevcut import'u güncelle:

```js
import { Stethoscope, FileText, X, Eye, Printer } from 'lucide-react';
```

- [ ] **Adım 2: Yeni state'leri ekle**

`MuayenePage.jsx` içindeki mevcut state tanımlarının hemen altına (satır 20 civarı) ekle:

```js
const [showSevkModal, setShowSevkModal] = useState(false);
const [sevkMuayene, setSevkMuayene] = useState(null);
```

- [ ] **Adım 3: `SevkKagidiModal` bileşenini ekle**

`MuayenePage` fonksiyonunun **dışına**, dosyanın en altına (son `}` kapanışından sonra) ekle:

```jsx
function SevkKagidiModal({ muayene, onClose }) {
  if (!muayene) return null;

  const hasta = muayene.randevu?.hasta;
  const doktor = muayene.randevu?.doktor;
  const tarih = muayene.muayeneTarihi
    ? new Date(muayene.muayeneTarihi).toLocaleDateString('tr-TR')
    : new Date().toLocaleDateString('tr-TR');
  const dogumTarihi = hasta?.dogumTarihi
    ? new Date(hasta.dogumTarihi).toLocaleDateString('tr-TR')
    : null;

  return (
    <div id="sevk-print-root">
      <div className="modal-overlay" onClick={onClose}>
        <div
          className="modal"
          style={{ maxWidth: 520, fontFamily: 'serif' }}
          onClick={e => e.stopPropagation()}
        >
          {/* Belge içeriği */}
          <div style={{ padding: '28px 32px' }}>
            {/* Başlık */}
            <div style={{ textAlign: 'center', borderBottom: '2px solid #0d9488', paddingBottom: 12, marginBottom: 16 }}>
              <div style={{ fontSize: 18, fontWeight: 700, letterSpacing: 1, color: '#0d9488' }}>
                ŞIFA POLİKLİNİĞİ
              </div>
              <div style={{ fontSize: 11, color: '#64748b', letterSpacing: 2, textTransform: 'uppercase', marginTop: 2 }}>
                Sevk Kâğıdı
              </div>
            </div>

            {/* Hasta bilgileri */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '6px 16px', fontSize: 13, marginBottom: 16 }}>
              <div><span style={{ color: '#64748b' }}>Hasta Adı Soyadı:</span><br /><strong>{hasta?.ad} {hasta?.soyad}</strong></div>
              <div><span style={{ color: '#64748b' }}>TC Kimlik No:</span><br /><strong>{hasta?.tcKimlik || '—'}</strong></div>
              {dogumTarihi && (
                <div><span style={{ color: '#64748b' }}>Doğum Tarihi:</span><br />{dogumTarihi}</div>
              )}
              <div><span style={{ color: '#64748b' }}>Sevk Tarihi:</span><br />{tarih}</div>
            </div>

            {/* Sevk edilen kurum */}
            <div style={{ border: '1px solid #cbd5e1', borderRadius: 6, padding: '10px 14px', marginBottom: 12, fontSize: 13 }}>
              <div style={{ color: '#64748b', fontSize: 11, marginBottom: 4 }}>Sevk Edilen Kurum / Bölüm:</div>
              <strong>{muayene.sevkKurumu}</strong>
            </div>

            {/* Tanı / sevk nedeni */}
            <div style={{ border: '1px solid #cbd5e1', borderRadius: 6, padding: '10px 14px', marginBottom: 20, fontSize: 13 }}>
              <div style={{ color: '#64748b', fontSize: 11, marginBottom: 4 }}>Tanı / Sevk Nedeni:</div>
              <span style={{ whiteSpace: 'pre-wrap' }}>{muayene.tani || '—'}</span>
            </div>

            {/* İmza */}
            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <div style={{ textAlign: 'center', borderTop: '1px solid #334155', paddingTop: 6, minWidth: 140 }}>
                <div style={{ fontSize: 13, fontWeight: 600 }}>
                  {doktor?.unvan} {doktor?.kullanici?.ad} {doktor?.kullanici?.soyad}
                </div>
                <div style={{ fontSize: 11, color: '#64748b' }}>
                  {doktor?.klinik?.ad || 'Klinik'}
                </div>
                <div style={{ fontSize: 10, color: '#94a3b8', marginTop: 2 }}>İmza / Kaşe</div>
              </div>
            </div>
          </div>

          {/* Butonlar — yazdırma sırasında CSS ile gizlenir */}
          <div className="modal-footer sevk-modal-actions" style={{ justifyContent: 'flex-end', gap: 8 }}>
            <button type="button" className="btn btn-secondary" onClick={onClose}>Kapat</button>
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => window.print()}
            >
              <Printer size={14} /> Yazdır / PDF
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
```

- [ ] **Adım 4: Modal'ı her iki return bloğuna bağla**

`MuayenePage.jsx`'te **iki ayrı return bloğu** var: admin için erken return (satır ~67) ve doktor için ana return (satır ~146). Her ikisine de şu snippet'i ekle — ilgili `return` içindeki en dıştaki `</div>`'dan hemen önce:

```jsx
{showSevkModal && sevkMuayene && (
  <SevkKagidiModal
    muayene={sevkMuayene}
    onClose={() => { setShowSevkModal(false); setSevkMuayene(null); }}
  />
)}
```

Admin return'ünün sonu şöyle görünmeli:
```jsx
        {/* ... showViewModal kodu ... */}
        {showSevkModal && sevkMuayene && (
          <SevkKagidiModal
            muayene={sevkMuayene}
            onClose={() => { setShowSevkModal(false); setSevkMuayene(null); }}
          />
        )}
      </div>  {/* en dıştaki admin div kapanışı */}
    );  // admin return kapanışı
  }
```

Doktor return'ünün sonu da aynı şekilde.

- [ ] **Adım 5: Commit**

```bash
git add frontend/src/pages/MuayenePage.jsx
git commit -m "feat: add SevkKagidiModal component with print support"
```

---

## Task 3: Görüntüleme modaline "Sevk Kâğıdı Yazdır" butonu

Bu buton, admin panelindeki `showViewModal` footer'ına eklenir. Yalnızca `sevkKurumu` dolu olduğunda görünür.

**Files:**
- Modify: `frontend/src/pages/MuayenePage.jsx` (satır 136-138 civarı)

- [ ] **Adım 1: Görüntüleme modalinin footer'ını bul**

`MuayenePage.jsx`'te şu kodu bul (satır ~136):

```jsx
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowViewModal(false)}>Kapat</button>
              </div>
```

- [ ] **Adım 2: Footer'ı güncelle**

Aşağıdakiyle değiştir:

```jsx
              <div className="modal-footer" style={{ justifyContent: 'space-between' }}>
                <div>
                  {selectedMuayene.sevkKurumu && (
                    <button
                      type="button"
                      className="btn btn-primary"
                      onClick={() => {
                        setSevkMuayene(selectedMuayene);
                        setShowViewModal(false);
                        setShowSevkModal(true);
                      }}
                    >
                      <Printer size={14} /> Sevk Kâğıdı Yazdır
                    </button>
                  )}
                </div>
                <button type="button" className="btn btn-secondary" onClick={() => setShowViewModal(false)}>Kapat</button>
              </div>
```

- [ ] **Adım 3: Manuel test**

1. Uygulamayı çalıştır: `cd frontend && npm run dev`
2. Admin olarak giriş yap → Muayene Kayıtları
3. `sevkKurumu` dolu bir muayene kaydının "Görüntüle" butonuna bas
4. Footer'da "Sevk Kâğıdı Yazdır" butonunun göründüğünü doğrula
5. Butona bas → `SevkKagidiModal` açılmalı, A şablonu görünmeli
6. "Yazdır / PDF" butonuna bas → tarayıcı baskı diyaloğu açılmalı, sadece sevk belgesi görünmeli
7. `sevkKurumu` **boş** bir kaydı aç → butonun **görünmediğini** doğrula

- [ ] **Adım 4: Commit**

```bash
git add frontend/src/pages/MuayenePage.jsx
git commit -m "feat: add sevk kagidi print button to view modal"
```

---

## Task 4: Muayene kaydı sonrası sevk kâğıdı teklifi

Doktor muayene kaydederken `sevkKurumu` doldurmuşsa, kayıt başarılı olduğunda otomatik olarak `SevkKagidiModal` açılır.

**Files:**
- Modify: `frontend/src/pages/MuayenePage.jsx` (handleSubmit fonksiyonu)

- [ ] **Adım 1: `handleSubmit`'i bul**

`MuayenePage.jsx`'te şu kodu bul (satır ~54):

```js
  const handleSubmit = async (e) => {
    e.preventDefault(); setError('');
    try {
      await muayeneAPI.create({
        randevuId: selectedRandevu.id,
        tani: form.tani, recete: form.recete, rapor: form.rapor, sevkKurumu: form.sevkKurumu
      });
      setShowModal(false); loadData();
    } catch (err) { setError(err.response?.data?.mesaj || 'Muayene kaydı oluşturulamadı.'); }
  };
```

- [ ] **Adım 2: `handleSubmit`'i güncelle**

Aşağıdakiyle değiştir:

```js
  const handleSubmit = async (e) => {
    e.preventDefault(); setError('');
    try {
      const res = await muayeneAPI.create({
        randevuId: selectedRandevu.id,
        tani: form.tani, recete: form.recete, rapor: form.rapor, sevkKurumu: form.sevkKurumu
      });
      setShowModal(false);
      if (form.sevkKurumu?.trim()) {
        // API kaydı döndürüyorsa onu kullan, yoksa form verisinden geçici nesne oluştur
        const kaydedilen = res?.data ?? {
          sevkKurumu: form.sevkKurumu,
          tani: form.tani,
          muayeneTarihi: new Date().toISOString(),
          randevu: selectedRandevu,
        };
        setSevkMuayene(kaydedilen);
        setShowSevkModal(true);
      }
      loadData();
    } catch (err) { setError(err.response?.data?.mesaj || 'Muayene kaydı oluşturulamadı.'); }
  };
```

- [ ] **Adım 3: Manuel test**

1. Doktor olarak giriş yap → Muayene Kayıtları → bekleyen bir randevu seç
2. Tanı doldur, Sevk Kurumu alanına "Test Hastanesi — Kardiyoloji" yaz
3. "Muayene Kaydet" butonuna bas
4. Muayene formu kapanmalı, hemen ardından `SevkKagidiModal` açılmalı
5. Belgede hasta adı, TC, sevk kurumu ve tanı doğru görünmeli
6. "Yazdır / PDF" → baskı diyaloğu, sadece belge görünmeli
7. "Kapat" → modal kapanmalı, liste yenilenmeli
8. **Sevk Kurumu boş bırakıldığında** kayıt yapıldıktan sonra modal **açılmamalı**

- [ ] **Adım 4: Commit**

```bash
git add frontend/src/pages/MuayenePage.jsx
git commit -m "feat: auto-open sevk kagidi modal after muayene submit when sevk kurumu filled"
```
