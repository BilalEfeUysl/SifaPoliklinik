import { useState, useEffect, useRef, useCallback } from 'react';
import { randevuAPI, hastaAPI, doktorAPI, klinikAPI } from '../api/axiosConfig';
import { useAuth } from '../context/AuthContext';
import { Plus, X, XCircle, CalendarDays, Edit2 } from 'lucide-react';

const EMPTY_FORM = { hastaId: '', hastaLabel: '', klinikId: '', doktorId: '', tarih: '', saat: '', notlar: '' };
const SAAT_LOADING = '__loading__';

export default function RandevuListPage() {
  const { hasRole, user } = useAuth();
  const isDoktor = hasRole('DOKTOR');

  const [randevular, setRandevular] = useState([]);
  const [klinikler, setKlinikler] = useState([]);
  const [filteredDoktorlar, setFilteredDoktorlar] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');
  const [form, setForm] = useState(EMPTY_FORM);
  const [musaitSaatler, setMusaitSaatler] = useState([]);
  const [saatYukleniyor, setSaatYukleniyor] = useState(false);
  const [klinikMusaitlik, setKlinikMusaitlik] = useState([]);
  const [klinikMusaitlikYukleniyor, setKlinikMusaitlikYukleniyor] = useState(false);
  const [alternatifTarihler, setAlternatifTarihler] = useState([]);
  const [alternatifYukleniyor, setAlternatifYukleniyor] = useState(false);

  // Doktor bilgisi (kendi doktor kaydı)
  const [doktorBilgi, setDoktorBilgi] = useState(null);

  // Hasta autocomplete
  const [hastaQuery, setHastaQuery] = useState('');
  const [hastaSuggestions, setHastaSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const autocompleteRef = useRef(null);
  const searchTimer = useRef(null);

  useEffect(() => { loadData(); }, []);

  useEffect(() => {
    if (form.doktorId && form.tarih) {
      setSaatYukleniyor(true);
      setMusaitSaatler([]);
      randevuAPI.getMusaitSaatler(form.doktorId, form.tarih)
        .then(res => {
          let saatler = Array.isArray(res.data) ? res.data : [];
          if (editingId && form.saat && !saatler.includes(form.saat)) {
            saatler = [...saatler, form.saat].sort();
          }
          setMusaitSaatler(saatler);
          if (saatler.length === 0 && form.klinikId) {
            setAlternatifYukleniyor(true);
            randevuAPI.getAlternatifTarihler(form.klinikId, form.tarih, 7)
              .then(altRes => setAlternatifTarihler(Array.isArray(altRes.data) ? altRes.data : []))
              .catch(() => setAlternatifTarihler([]))
              .finally(() => setAlternatifYukleniyor(false));
          } else {
            setAlternatifTarihler([]);
          }
        })
        .catch(() => setMusaitSaatler([]))
        .finally(() => setSaatYukleniyor(false));
    } else {
      setMusaitSaatler([]);
      setAlternatifTarihler([]);
      setSaatYukleniyor(false);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [form.doktorId, form.tarih, editingId]);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (autocompleteRef.current && !autocompleteRef.current.contains(e.target)) {
        setShowSuggestions(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    if (form.klinikId && form.tarih && !form.doktorId) {
      setKlinikMusaitlikYukleniyor(true);
      setKlinikMusaitlik([]);
      randevuAPI.getKlinikMusaitSaatler(form.klinikId, form.tarih)
        .then(res => setKlinikMusaitlik(Array.isArray(res.data) ? res.data : []))
        .catch(() => setKlinikMusaitlik([]))
        .finally(() => setKlinikMusaitlikYukleniyor(false));
    } else {
      setKlinikMusaitlik([]);
    }
  }, [form.klinikId, form.tarih, form.doktorId]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [kRes] = await Promise.all([klinikAPI.getAll()]);
      setKlinikler(kRes.data);

      if (isDoktor) {
        // Doktorun kendi kaydını bul
        const doktorlarRes = await doktorAPI.getAll().catch(() => ({ data: [] }));
        const bulunan = doktorlarRes.data.find(
          d => d.kullanici?.id === user.id || d.kullanici?.email === user.email
        );
        setDoktorBilgi(bulunan || null);

        if (bulunan) {
          const rRes = await randevuAPI.getByDoktor(bulunan.id).catch(() => ({ data: [] }));
          const list = Array.isArray(rRes.data) ? rRes.data : [];
          setRandevular(list.sort((a, b) => new Date(b.tarihSaat) - new Date(a.tarihSaat)));
        }
      } else {
        const rRes = await randevuAPI.getAll();
        setRandevular(rRes.data);
      }
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const handleHastaQueryChange = useCallback((val) => {
    setHastaQuery(val);
    setForm(f => ({ ...f, hastaId: '', hastaLabel: '' }));
    clearTimeout(searchTimer.current);
    if (val.trim().length < 2) { setHastaSuggestions([]); setShowSuggestions(false); return; }
    searchTimer.current = setTimeout(async () => {
      try {
        const res = await hastaAPI.search(val.trim());
        const list = Array.isArray(res.data) ? res.data : (res.data?.content || []);
        setHastaSuggestions(list);
        setShowSuggestions(true);
      } catch { setHastaSuggestions([]); }
    }, 300);
  }, []);

  const selectHasta = (h) => {
    const label = `${h.ad} ${h.soyad} (${h.tcKimlik})`;
    setHastaQuery(label);
    setForm(f => ({ ...f, hastaId: String(h.id), hastaLabel: label }));
    setShowSuggestions(false);
  };

  const handleKlinikChange = async (klinikId) => {
    setForm(f => ({ ...f, klinikId, doktorId: '', saat: '' }));
    setMusaitSaatler([]);
    if (!klinikId) { setFilteredDoktorlar([]); return; }
    try {
      const res = await doktorAPI.getByKlinik(klinikId);
      setFilteredDoktorlar(res.data);
    } catch { setFilteredDoktorlar([]); }
  };

  const openCreateModal = () => {
    // Doktor rolü randevu oluşturamaz
    if (isDoktor) return;
    setError('');
    setEditingId(null);
    setForm(EMPTY_FORM);
    setHastaQuery('');
    setHastaSuggestions([]);
    setFilteredDoktorlar([]);
    setMusaitSaatler([]);
    setShowModal(true);
  };

  const openEditModal = async (r) => {
    setError('');
    setEditingId(r.id);
    const klinikId = String(r.doktor?.klinik?.id || '');
    const hastaLabel = `${r.hasta?.ad} ${r.hasta?.soyad} (${r.hasta?.tcKimlik})`;
    const tarihSaat = new Date(r.tarihSaat);
    const tarih = tarihSaat.toISOString().split('T')[0];
    // Lokale bağımsız saat formatı: toLocaleTimeString tr-TR bazen '09.00' (noktalı) üretebiliyor
    const saat = `${String(tarihSaat.getHours()).padStart(2, '0')}:${String(tarihSaat.getMinutes()).padStart(2, '0')}`;

    setHastaQuery(hastaLabel);
    setForm({
      hastaId: String(r.hasta?.id || ''),
      hastaLabel,
      klinikId,
      doktorId: String(r.doktor?.id || ''),
      tarih,
      saat,
      notlar: r.notlar || '',
    });

    if (klinikId) {
      try {
        const res = await doktorAPI.getByKlinik(klinikId);
        setFilteredDoktorlar(res.data);
      } catch { setFilteredDoktorlar([]); }
    }
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault(); setError('');
    if (!form.hastaId) { setError('Lütfen listeden bir hasta seçiniz.'); return; }
    if (!form.tarih || !form.saat) { setError('Lütfen tarih ve saat seçiniz.'); return; }
    const payload = {
      hastaId: parseInt(form.hastaId),
      doktorId: parseInt(form.doktorId),
      tarihSaat: `${form.tarih}T${form.saat}`,
      notlar: form.notlar,
    };
    try {
      if (editingId) {
        await randevuAPI.update(editingId, payload);
      } else {
        await randevuAPI.create(payload);
      }
      setShowModal(false);
      loadData();
    } catch (err) { setError(err.response?.data?.mesaj || 'İşlem başarısız.'); }
  };

  const handleCancel = async (id) => {
    if (!confirm('Bu randevuyu iptal etmek istediğinize emin misiniz?')) return;
    try { await randevuAPI.cancel(id); loadData(); }
    catch (err) { setError(err.response?.data?.mesaj || 'İptal başarısız.'); }
  };

  if (loading) return <div className="empty-state"><p>Yükleniyor...</p></div>;

  // Doktor: kendi randevularını yönetebilir (düzenle + iptal)
  // Randevu Görevlisi: yeni oluşturabilir + düzenleyebilir + iptal edebilir
  const canCreate = hasRole('RANDEVU_GOREVLISI');
  const canEdit   = hasRole('RANDEVU_GOREVLISI') || isDoktor;
  const canCancel = hasRole('RANDEVU_GOREVLISI') || isDoktor;

  return (
    <div>
      <div className="page-header">
        <div>
          <h2>Randevu Yönetimi</h2>
          {isDoktor && doktorBilgi && (
            <p style={{ fontSize: 13, color: 'var(--gray-500)', marginTop: 4 }}>
              Yalnızca size atanmış randevular gösterilmektedir.
            </p>
          )}
        </div>
        {canCreate && (
          <button className="btn btn-primary" onClick={openCreateModal}>
            <Plus size={16} /> Yeni Randevu
          </button>
        )}
      </div>

      {error && (
        <div style={{ background: 'var(--danger-bg)', color: '#991b1b', padding: '10px 14px', borderRadius: 6, fontSize: 13, marginBottom: 16, display: 'flex', alignItems: 'center', gap: 8 }}>
          <span>{error}</span>
          <button onClick={() => setError('')} style={{ marginLeft: 'auto', background: 'none', border: 'none', cursor: 'pointer', color: 'inherit' }}>✕</button>
        </div>
      )}

      {/* Doktor ancak kendi kaydı bulunamazsa uyarı */}
      {isDoktor && !doktorBilgi && !loading && (
        <div style={{ background: 'var(--warning-bg, #fefce8)', color: '#854d0e', padding: '12px 16px', borderRadius: 8, fontSize: 13, marginBottom: 16, border: '1px solid #fde047' }}>
          ⚠️ Hesabınıza bağlı doktor kaydı bulunamadı. Yöneticiyle iletişime geçin.
        </div>
      )}

      <div className="card">
        <div className="card-body" style={{ padding: 0 }}>
          {randevular.length === 0 ? (
            <div className="empty-state">
              <CalendarDays size={48} />
              <h4>{isDoktor ? 'Size atanmış randevu yok' : 'Henüz randevu yok'}</h4>
            </div>
          ) : (
            <div className="data-table-wrapper">
              <table className="data-table">
                <thead><tr>
                  <th>ID</th><th>Hasta</th>
                  {!isDoktor && <th>Doktor</th>}
                  <th>Klinik</th><th>Tarih/Saat</th><th>Durum</th><th>İşlem</th>
                </tr></thead>
                <tbody>
                  {randevular.map(r => (
                    <tr key={r.id}>
                      <td>#{r.id}</td>
                      <td style={{ fontWeight: 600 }}>{r.hasta?.ad} {r.hasta?.soyad}</td>
                      {!isDoktor && <td>{r.doktor?.unvan} {r.doktor?.kullanici?.ad} {r.doktor?.kullanici?.soyad}</td>}
                      <td>{r.doktor?.klinik?.ad || '-'}</td>
                      <td>{new Date(r.tarihSaat).toLocaleString('tr-TR')}</td>
                      <td>
                        <span className={`badge ${r.durum === 'TAMAMLANDI' ? 'badge-success' : r.durum === 'IPTAL' ? 'badge-danger' : 'badge-warning'}`}>
                          <span className={`badge-dot ${r.durum === 'TAMAMLANDI' ? 'green' : r.durum === 'IPTAL' ? 'red' : 'yellow'}`}></span>
                          {r.durum === 'TAMAMLANDI' ? 'Tamamlandı' : r.durum === 'IPTAL' ? 'İptal' : 'Bekliyor'}
                        </span>
                      </td>
                      <td style={{ display: 'flex', gap: 6 }}>
                        {r.durum === 'BEKLIYOR' && canEdit && (
                          <button className="btn btn-sm btn-secondary" onClick={() => openEditModal(r)}
                            style={{ fontSize: 11, padding: '4px 10px' }}>
                            <Edit2 size={12} /> Düzenle
                          </button>
                        )}
                        {r.durum === 'BEKLIYOR' && canCancel && (
                          <button className="btn btn-sm btn-danger" onClick={() => handleCancel(r.id)}
                            style={{ fontSize: 11, padding: '4px 10px' }}>
                            <XCircle size={12} /> İptal
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" style={{ maxWidth: 620 }} onClick={e => e.stopPropagation()}>

            {/* Modal Header */}
            <div className="modal-header" style={{ background: 'linear-gradient(135deg, #0f172a, #1e293b)', borderRadius: '16px 16px 0 0', padding: '20px 24px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                <div style={{ width: 38, height: 38, borderRadius: 10, background: 'linear-gradient(135deg, var(--primary-400), var(--primary-600))', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                  <CalendarDays size={18} color="#fff" />
                </div>
                <div>
                  <h3 style={{ color: '#fff', margin: 0, fontSize: 16 }}>{editingId ? 'Randevu Düzenle' : 'Yeni Randevu Oluştur'}</h3>
                  <p style={{ color: 'var(--gray-400)', fontSize: 12, margin: 0 }}>{editingId ? `Randevu #${editingId}` : 'Aşağıdaki alanları doldurun'}</p>
                </div>
              </div>
              <button className="btn-icon" onClick={() => setShowModal(false)} style={{ background: 'rgba(255,255,255,0.1)', border: 'none', color: '#fff' }}><X size={18} /></button>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="modal-body">
                {error && (
                  <div style={{ background: 'var(--danger-bg)', color: '#991b1b', padding: '10px 14px', borderRadius: 8, fontSize: 13, marginBottom: 16, border: '1px solid #fca5a5', display: 'flex', alignItems: 'center', gap: 8 }}>
                    ⚠️ {error}
                  </div>
                )}

                {/* Hasta Autocomplete */}
                <div className="form-group" ref={autocompleteRef} style={{ position: 'relative' }}>
                  <label className="form-label">Hasta *</label>
                  <div style={{ position: 'relative' }}>
                    <input
                      className="form-input"
                      type="text"
                      placeholder="Ad, soyad veya TC kimlik ile ara..."
                      value={hastaQuery}
                      onChange={e => handleHastaQueryChange(e.target.value)}
                      onFocus={() => hastaSuggestions.length > 0 && setShowSuggestions(true)}
                      autoComplete="off"
                      required={!form.hastaId}
                      style={form.hastaId ? { borderColor: 'var(--success)', background: '#f0fdf4', paddingRight: 36 } : {}}
                    />
                    {form.hastaId && (
                      <span style={{ position: 'absolute', right: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--success)', fontWeight: 700, fontSize: 16 }}>✓</span>
                    )}
                  </div>
                  {showSuggestions && hastaSuggestions.length > 0 && (
                    <ul style={{ position: 'absolute', top: 'calc(100% + 4px)', left: 0, right: 0, zIndex: 200, background: '#fff', border: '1.5px solid var(--primary-300)', borderRadius: 10, boxShadow: '0 8px 24px rgba(0,0,0,0.12)', listStyle: 'none', margin: 0, padding: '6px 0', maxHeight: 200, overflowY: 'auto' }}>
                      {hastaSuggestions.map(h => (
                        <li key={h.id}
                          onMouseDown={() => selectHasta(h)}
                          style={{ padding: '10px 14px', cursor: 'pointer', fontSize: 13 }}
                          onMouseEnter={e => e.currentTarget.style.background = 'var(--primary-50)'}
                          onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
                        >
                          <span style={{ fontWeight: 600 }}>{h.ad} {h.soyad}</span>
                          <span style={{ color: 'var(--gray-400)', marginLeft: 6, fontSize: 12 }}>({h.tcKimlik})</span>
                        </li>
                      ))}
                    </ul>
                  )}
                  {hastaSuggestions.length === 0 && hastaQuery.trim().length >= 2 && !showSuggestions && !form.hastaId && (
                    <div style={{ background: '#fef3c7', border: '1.5px solid #fde68a', borderRadius: 8, padding: '10px 14px', marginTop: 6, fontSize: 12, color: '#92400e' }}>
                      ⚠️ Bu isim/TC ile kayıtlı hasta bulunamadı. Lütfen hastayı <strong>kayıt görevlisine yönlendirin</strong> ve kayıt sonrası randevuya geri dönün.
                      {(hasRole('KAYIT_GOREVLISI') || hasRole('YONETICI')) && (
                        <a href="/hastalar" style={{ display: 'block', marginTop: 6, color: '#1d4ed8', fontWeight: 600, textDecoration: 'underline' }}>→ Hasta kaydına git</a>
                      )}
                    </div>
                  )}
                </div>

                {/* Klinik + Doktor — 2 sütun */}
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14, marginBottom: 18 }}>
                  <div>
                    <label className="form-label">Klinik *</label>
                    <select className="form-select" value={form.klinikId} onChange={e => handleKlinikChange(e.target.value)} required>
                      <option value="">Klinik seçin...</option>
                      {klinikler.map(k => <option key={k.id} value={k.id}>{k.ad}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="form-label">Doktor *</label>
                    <select className="form-select" value={form.doktorId}
                      onChange={e => setForm(f => ({ ...f, doktorId: e.target.value, saat: '' }))}
                      required disabled={!form.klinikId}>
                      <option value="">{!form.klinikId ? 'Önce klinik seçin' : 'Doktor seçin...'}</option>
                      {filteredDoktorlar.map(d => (
                        <option key={d.id} value={d.id}>{d.unvan} {d.kullanici?.ad} {d.kullanici?.soyad}</option>
                      ))}
                    </select>
                  </div>
                </div>

                {form.klinikId && form.tarih && !form.doktorId && (
                  <div style={{ marginBottom: 18 }}>
                    <label className="form-label">Klinikteki Müsait Doktor / Saatler</label>
                    {klinikMusaitlikYukleniyor ? (
                      <div style={{ fontSize: 12, color: 'var(--gray-400)', padding: '8px 0' }}>Yükleniyor...</div>
                    ) : klinikMusaitlik.length === 0 ? (
                      <div style={{ background: '#fef3c7', border: '1.5px solid #fde68a', borderRadius: 8, padding: '10px 14px', fontSize: 12, color: '#92400e' }}>
                        Bu tarihte klinikte müsait doktor bulunmuyor.
                      </div>
                    ) : (
                      klinikMusaitlik.map(dk => (
                        <div key={dk.doktorId} style={{ marginBottom: 10 }}>
                          <div style={{ fontSize: 12, fontWeight: 600, color: 'var(--gray-600)', marginBottom: 4 }}>{dk.doktorAd}</div>
                          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                            {dk.musaitSaatler.map(s => (
                              <button
                                key={s}
                                type="button"
                                onClick={() => setForm(f => ({ ...f, doktorId: String(dk.doktorId), saat: s }))}
                                style={{ padding: '5px 12px', borderRadius: 6, border: '1.5px solid var(--primary-300)', background: 'var(--primary-50)', color: 'var(--primary-700)', fontSize: 12, fontWeight: 600, cursor: 'pointer' }}
                              >
                                {s}
                              </button>
                            ))}
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                )}

                {/* Tarih */}
                <div className="form-group">
                  <label className="form-label">Tarih *</label>
                  <input className="form-input" type="date" value={form.tarih}
                    min={new Date().toISOString().split('T')[0]}
                    onChange={e => setForm(f => ({ ...f, tarih: e.target.value, saat: '' }))} required />
                </div>

                {/* Müsait Saatler — buton grid */}
                <div className="form-group">
                  <label className="form-label" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <span>Müsait Saatler *</span>
                    {musaitSaatler.length > 0 && (
                      <span style={{ fontSize: 11, color: 'var(--primary-600)', background: 'var(--primary-50)', padding: '2px 8px', borderRadius: 99, fontWeight: 500 }}>
                        {musaitSaatler.length} slot
                      </span>
                    )}
                  </label>

                  {saatYukleniyor ? (
                    <div style={{ background: 'var(--gray-50)', border: '1.5px dashed var(--gray-200)', borderRadius: 10, padding: 20, textAlign: 'center', color: 'var(--gray-400)', fontSize: 13 }}>
                      Saatler yükleniyor...
                    </div>
                  ) : (!form.doktorId || !form.tarih) ? (
                    <div style={{ background: 'var(--gray-50)', border: '1.5px dashed var(--gray-200)', borderRadius: 10, padding: 20, textAlign: 'center', color: 'var(--gray-400)', fontSize: 13 }}>
                      <CalendarDays size={20} style={{ marginBottom: 4, opacity: 0.4 }} />
                      <p style={{ margin: 0 }}>Doktor ve tarih seçilince saatler görünür</p>
                    </div>
                  ) : musaitSaatler.length === 0 ? (
                    <div>
                      <div style={{ background: '#fef3c7', border: '1.5px solid #fde68a', borderRadius: 10, padding: 16, textAlign: 'center', color: '#92400e', fontSize: 13 }}>
                        ⚠️ Bu tarihte seçili doktor için müsait saat bulunmuyor
                      </div>
                      {alternatifYukleniyor && (
                        <div style={{ fontSize: 12, color: 'var(--gray-400)', marginTop: 8, textAlign: 'center' }}>Alternatif tarihler aranıyor...</div>
                      )}
                      {!alternatifYukleniyor && alternatifTarihler.length > 0 && (
                        <div style={{ marginTop: 12 }}>
                          <div style={{ fontSize: 12, fontWeight: 700, color: 'var(--gray-600)', marginBottom: 8 }}>Önerilen Alternatif Tarihler</div>
                          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                            {alternatifTarihler.map((a, i) => {
                              const dt = new Date(a.tarihSaat);
                              const tarihStr = dt.toLocaleDateString('tr-TR');
                              const saatStr = `${String(dt.getHours()).padStart(2,'0')}:${String(dt.getMinutes()).padStart(2,'0')}`;
                              return (
                                <button
                                  key={i}
                                  type="button"
                                  onClick={() => {
                                    const tarihISO = dt.toISOString().split('T')[0];
                                    setForm(f => ({ ...f, doktorId: String(a.doktorId), tarih: tarihISO, saat: saatStr }));
                                  }}
                                  style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '10px 14px', borderRadius: 8, border: '1.5px solid var(--primary-200)', background: 'var(--primary-50)', cursor: 'pointer', fontSize: 12 }}
                                >
                                  <span style={{ fontWeight: 600, color: 'var(--primary-700)' }}>{a.doktorAd}</span>
                                  <span style={{ color: 'var(--gray-600)' }}>{tarihStr} — {saatStr}</span>
                                </button>
                              );
                            })}
                          </div>
                        </div>
                      )}
                    </div>
                  ) : (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 8 }}>
                      {musaitSaatler.map(s => (
                        <button
                          key={s}
                          type="button"
                          onClick={() => setForm(f => ({ ...f, saat: s }))}
                          style={{
                            padding: '9px 4px',
                            borderRadius: 8,
                            border: form.saat === s ? '2px solid var(--primary-500)' : '1.5px solid var(--gray-200)',
                            background: form.saat === s ? 'linear-gradient(135deg, var(--primary-500), var(--primary-600))' : '#fff',
                            color: form.saat === s ? '#fff' : 'var(--gray-700)',
                            fontSize: 13,
                            fontWeight: form.saat === s ? 700 : 500,
                            cursor: 'pointer',
                            transition: 'all 0.15s ease',
                            boxShadow: form.saat === s ? '0 2px 8px rgba(13,148,136,0.3)' : '0 1px 2px rgba(0,0,0,0.04)',
                          }}
                        >
                          {s}
                        </button>
                      ))}
                    </div>
                  )}
                </div>

                {/* Notlar */}
                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label className="form-label">Notlar <span style={{ fontWeight: 400, color: 'var(--gray-400)' }}>(isteğe bağlı)</span></label>
                  <textarea className="form-textarea" value={form.notlar} rows={2}
                    placeholder="Randevu ile ilgili ek notlar..."
                    onChange={e => setForm(f => ({ ...f, notlar: e.target.value }))}
                    style={{ minHeight: 68 }}
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>İptal</button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={!form.hastaId || !form.doktorId || !form.tarih || !form.saat}
                  style={{ opacity: (!form.hastaId || !form.doktorId || !form.tarih || !form.saat) ? 0.5 : 1, cursor: (!form.hastaId || !form.doktorId || !form.tarih || !form.saat) ? 'not-allowed' : 'pointer' }}
                >
                  {editingId ? '✓ Güncelle' : '+ Randevu Oluştur'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
