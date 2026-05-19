import { useState, useEffect } from 'react';
import { hastaAPI, muayeneAPI, randevuAPI } from '../api/axiosConfig';
import { useAuth } from '../context/AuthContext';
import {
  Plus, Search, Edit2, Trash2, AlertCircle, X,
  History, User, CalendarDays, FileText, Eye
} from 'lucide-react';

/* ─────────────────────────────────────────────
   Doktor: Hasta Geçmişi Modal
───────────────────────────────────────────── */
function HastaGecmisiModal({ hasta, onClose }) {
  const [aktifSekme, setAktifSekme] = useState('muayeneler');
  const [muayeneler, setMuayeneler] = useState([]);
  const [randevular, setRandevular] = useState([]);
  const [loading, setLoading] = useState(true);
  const [secilenMuayene, setSecilenMuayene] = useState(null);

  useEffect(() => {
    const load = async () => {
      try {
        const [mRes, rRes] = await Promise.all([
          muayeneAPI.getByHasta(hasta.id).catch(() => ({ data: [] })),
          randevuAPI.getByHasta(hasta.id).catch(() => ({ data: [] })),
        ]);
        const mList = Array.isArray(mRes.data) ? mRes.data : [];
        const rList = Array.isArray(rRes.data) ? rRes.data : [];
        setMuayeneler(mList.sort((a, b) => new Date(b.muayeneTarihi || 0) - new Date(a.muayeneTarihi || 0)));
        setRandevular(rList.sort((a, b) => new Date(b.tarihSaat || 0) - new Date(a.tarihSaat || 0)));
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [hasta.id]);

  const TAB_STYLE = (aktif) => ({
    padding: '10px 20px',
    border: 'none',
    borderBottom: aktif ? '2px solid var(--primary)' : '2px solid transparent',
    background: 'transparent',
    color: aktif ? 'var(--primary)' : 'var(--gray-500)',
    fontWeight: aktif ? 700 : 500,
    fontSize: 14,
    cursor: 'pointer',
    transition: 'all 0.15s',
  });

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" style={{ maxWidth: 760, maxHeight: '85vh', display: 'flex', flexDirection: 'column' }}
        onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <h3 style={{ margin: 0 }}>
              <User size={16} style={{ verticalAlign: 'middle', marginRight: 6 }} />
              {hasta.ad} {hasta.soyad}
            </h3>
            <p style={{ fontSize: 12, color: 'var(--gray-500)', margin: '4px 0 0' }}>
              TC: {hasta.tcKimlik} &nbsp;|&nbsp; {hasta.telefon || 'Telefon yok'} &nbsp;|&nbsp; SGK: {hasta.sgkDurumu === 'AKTIF' ? 'Aktif' : 'Pasif'}
            </p>
          </div>
          <button className="btn-icon" onClick={onClose}><X size={18} /></button>
        </div>

        {/* Sekmeler */}
        <div style={{ display: 'flex', borderBottom: '1px solid var(--border-color)', flexShrink: 0 }}>
          <button style={TAB_STYLE(aktifSekme === 'muayeneler')}
            onClick={() => { setAktifSekme('muayeneler'); setSecilenMuayene(null); }}>
            <FileText size={14} style={{ verticalAlign: 'middle', marginRight: 6 }} />
            Geçmiş Muayeneler
            {muayeneler.length > 0 && (
              <span className="badge badge-success" style={{ marginLeft: 8, fontSize: 11 }}>{muayeneler.length}</span>
            )}
          </button>
          <button style={TAB_STYLE(aktifSekme === 'randevular')}
            onClick={() => { setAktifSekme('randevular'); setSecilenMuayene(null); }}>
            <CalendarDays size={14} style={{ verticalAlign: 'middle', marginRight: 6 }} />
            Randevu Geçmişi
            {randevular.length > 0 && (
              <span className="badge badge-warning" style={{ marginLeft: 8, fontSize: 11 }}>{randevular.length}</span>
            )}
          </button>
        </div>

        {/* İçerik */}
        <div style={{ overflowY: 'auto', flex: 1 }}>
          {loading ? (
            <div className="empty-state"><p>Yükleniyor...</p></div>
          ) : aktifSekme === 'muayeneler' ? (
            secilenMuayene ? (
              /* Muayene detayı */
              <div style={{ padding: 24 }}>
                <button className="btn btn-secondary" style={{ marginBottom: 16, fontSize: 12 }}
                  onClick={() => setSecilenMuayene(null)}>
                  ← Listeye Dön
                </button>
                <div style={{ background: 'var(--gray-50)', borderRadius: 10, padding: 16, marginBottom: 16 }}>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, fontSize: 13 }}>
                    <div><strong>Doktor:</strong> {secilenMuayene.randevu?.doktor?.unvan} {secilenMuayene.randevu?.doktor?.kullanici?.ad} {secilenMuayene.randevu?.doktor?.kullanici?.soyad}</div>
                    <div><strong>Klinik:</strong> {secilenMuayene.randevu?.doktor?.klinik?.ad || '-'}</div>
                    <div><strong>Muayene Tarihi:</strong> {secilenMuayene.muayeneTarihi ? new Date(secilenMuayene.muayeneTarihi).toLocaleString('tr-TR') : '-'}</div>
                  </div>
                </div>
                {[
                  { label: 'Tanı / Teşhis', value: secilenMuayene.tani },
                  { label: 'Reçete (İlaçlar)', value: secilenMuayene.recete },
                  { label: 'Rapor', value: secilenMuayene.rapor },
                  { label: 'Sevk Kurumu', value: secilenMuayene.sevkKurumu },
                ].map(({ label, value }) => value ? (
                  <div className="form-group" key={label}>
                    <label className="form-label">{label}</label>
                    <div style={{ padding: '10px 12px', background: 'var(--gray-50)', borderRadius: 6, fontSize: 13, whiteSpace: 'pre-wrap' }}>
                      {value}
                    </div>
                  </div>
                ) : null)}
              </div>
            ) : (
              /* Muayene listesi */
              muayeneler.length === 0 ? (
                <div className="empty-state">
                  <History size={40} />
                  <h4>Geçmiş muayene kaydı yok</h4>
                  <p>Bu hastanın henüz muayene kaydı bulunmamaktadır.</p>
                </div>
              ) : (
                <div className="data-table-wrapper">
                  <table className="data-table">
                    <thead><tr>
                      <th>Tarih</th><th>Doktor</th><th>Klinik</th><th>Tanı</th><th>İşlem</th>
                    </tr></thead>
                    <tbody>
                      {muayeneler.map(m => (
                        <tr key={m.id}>
                          <td style={{ whiteSpace: 'nowrap' }}>
                            {m.muayeneTarihi ? new Date(m.muayeneTarihi).toLocaleString('tr-TR') : '-'}
                          </td>
                          <td>{m.randevu?.doktor?.unvan} {m.randevu?.doktor?.kullanici?.ad}</td>
                          <td>{m.randevu?.doktor?.klinik?.ad || '-'}</td>
                          <td style={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                            {m.tani || '-'}
                          </td>
                          <td>
                            <button className="btn btn-sm btn-secondary"
                              style={{ fontSize: 11, padding: '4px 10px' }}
                              onClick={() => setSecilenMuayene(m)}>
                              <Eye size={12} /> Detay
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )
            )
          ) : (
            /* Randevu geçmişi */
            randevular.length === 0 ? (
              <div className="empty-state">
                <CalendarDays size={40} />
                <h4>Randevu geçmişi yok</h4>
              </div>
            ) : (
              <div className="data-table-wrapper">
                <table className="data-table">
                  <thead><tr>
                    <th>Tarih/Saat</th><th>Doktor</th><th>Klinik</th><th>Durum</th>
                  </tr></thead>
                  <tbody>
                    {randevular.map(r => (
                      <tr key={r.id}>
                        <td style={{ whiteSpace: 'nowrap' }}>{new Date(r.tarihSaat).toLocaleString('tr-TR')}</td>
                        <td>{r.doktor?.unvan} {r.doktor?.kullanici?.ad} {r.doktor?.kullanici?.soyad}</td>
                        <td>{r.doktor?.klinik?.ad || '-'}</td>
                        <td>
                          <span className={`badge ${r.durum === 'TAMAMLANDI' ? 'badge-success' : r.durum === 'IPTAL' ? 'badge-danger' : 'badge-warning'}`}>
                            <span className={`badge-dot ${r.durum === 'TAMAMLANDI' ? 'green' : r.durum === 'IPTAL' ? 'red' : 'yellow'}`}></span>
                            {r.durum === 'TAMAMLANDI' ? 'Tamamlandı' : r.durum === 'IPTAL' ? 'İptal' : 'Bekliyor'}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )
          )}
        </div>

        <div className="modal-footer">
          <button type="button" className="btn btn-secondary" onClick={onClose}>Kapat</button>
        </div>
      </div>
    </div>
  );
}

/* ─────────────────────────────────────────────
   Ana HastaListPage
───────────────────────────────────────────── */
export default function HastaListPage() {
  const { hasRole } = useAuth();
  const isDoktor = hasRole('DOKTOR');

  const [hastalar, setHastalar] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingHasta, setEditingHasta] = useState(null);
  const [gecmisHasta, setGecmisHasta] = useState(null); // geçmiş modal
  const [error, setError] = useState('');
  const [sayfa, setSayfa] = useState(0);
  const [toplamSayfa, setToplamSayfa] = useState(0);
  const [toplamKayit, setToplamKayit] = useState(0);
  const PAGE_SIZE = 10;
  const [form, setForm] = useState({
    tcKimlik: '', ad: '', soyad: '', dogumTarihi: '',
    telefon: '', adres: '', sgkDurumu: 'AKTIF'
  });

  useEffect(() => { loadHastalar(0); }, []);

  const loadHastalar = async (sayfaNo = 0) => {
    setLoading(true);
    try {
      const res = await hastaAPI.getAll(sayfaNo, PAGE_SIZE);
      if (res.data.content) {
        setHastalar(res.data.content);
        setToplamSayfa(res.data.totalPages);
        setToplamKayit(res.data.totalElements);
        setSayfa(sayfaNo);
      } else {
        setHastalar(res.data);
      }
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const openNew = () => {
    setEditingHasta(null);
    setForm({ tcKimlik: '', ad: '', soyad: '', dogumTarihi: '', telefon: '', adres: '', sgkDurumu: 'AKTIF' });
    setError(''); setShowModal(true);
  };

  const openEdit = (h) => {
    setEditingHasta(h);
    setForm({
      tcKimlik: h.tcKimlik, ad: h.ad, soyad: h.soyad,
      dogumTarihi: h.dogumTarihi || '', telefon: h.telefon || '',
      adres: h.adres || '', sgkDurumu: h.sgkDurumu || 'AKTIF'
    });
    setError(''); setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault(); setError('');
    try {
      if (editingHasta) await hastaAPI.update(editingHasta.id, form);
      else await hastaAPI.create(form);
      setShowModal(false); loadHastalar(sayfa);
    } catch (err) { setError(err.response?.data?.mesaj || 'İşlem başarısız.'); }
  };

  const handleDelete = async (id) => {
    if (!confirm('Bu hastayı silmek istediğinize emin misiniz?')) return;
    try { await hastaAPI.delete(id); loadHastalar(sayfa); }
    catch (err) { alert(err.response?.data?.mesaj || 'Silme başarısız.'); }
  };

  const filtered = hastalar.filter(h =>
    !search || h.ad.toLowerCase().includes(search.toLowerCase()) ||
    h.soyad.toLowerCase().includes(search.toLowerCase()) || h.tcKimlik.includes(search)
  );

  if (loading) return <div className="empty-state"><p>Yükleniyor...</p></div>;

  return (
    <div>
      <div className="page-header">
        <h2>Hasta {isDoktor ? 'Sorgulama' : 'Yönetimi'}</h2>
        {hasRole('KAYIT_GOREVLISI', 'YONETICI') && (
          <button className="btn btn-primary" onClick={openNew}><Plus size={16} /> Yeni Hasta</button>
        )}
      </div>

      <div style={{ marginBottom: 20 }}>
        <div className="search-bar" style={{ maxWidth: 400 }}>
          <Search size={16} />
          <input placeholder="Ad, soyad veya TC ile ara..." value={search}
            onChange={(e) => setSearch(e.target.value)} />
        </div>
      </div>

      <div className="card">
        <div className="card-body" style={{ padding: 0 }}>
          {filtered.length === 0 ? (
            <div className="empty-state"><AlertCircle size={48} /><h4>Hasta bulunamadı</h4></div>
          ) : (
            <div className="data-table-wrapper">
              <table className="data-table">
                <thead><tr>
                  <th>TC Kimlik</th><th>Ad Soyad</th><th>Telefon</th><th>SGK</th><th>Kayıt Tarihi</th><th>İşlem</th>
                </tr></thead>
                <tbody>
                  {filtered.map(h => (
                    <tr key={h.id}>
                      <td style={{ fontFamily: 'monospace' }}>{h.tcKimlik}</td>
                      <td style={{ fontWeight: 600 }}>{h.ad} {h.soyad}</td>
                      <td>{h.telefon || '-'}</td>
                      <td>
                        <span className={`badge ${h.sgkDurumu === 'AKTIF' ? 'badge-success' : 'badge-danger'}`}>
                          <span className={`badge-dot ${h.sgkDurumu === 'AKTIF' ? 'green' : 'red'}`}></span>
                          {h.sgkDurumu === 'AKTIF' ? 'Aktif' : 'Pasif'}
                        </span>
                      </td>
                      <td>{h.kayitTarihi ? new Date(h.kayitTarihi).toLocaleDateString('tr-TR') : '-'}</td>
                      <td>
                        <div style={{ display: 'flex', gap: 6 }}>
                          {/* Doktor: Geçmiş Görüntüle */}
                          {isDoktor && (
                            <button
                              className="btn btn-sm btn-secondary"
                              style={{ fontSize: 11, padding: '4px 10px', display: 'flex', alignItems: 'center', gap: 4 }}
                              onClick={() => setGecmisHasta(h)}
                            >
                              <History size={12} /> Geçmiş
                            </button>
                          )}
                          {/* Düzenle */}
                          {hasRole('KAYIT_GOREVLISI', 'YONETICI', 'DOKTOR') && (
                            <button className="btn-icon btn-sm" onClick={() => openEdit(h)}><Edit2 size={14} /></button>
                          )}
                          {/* Sil */}
                          {hasRole('KAYIT_GOREVLISI', 'YONETICI') && (
                            <button className="btn-icon btn-sm" onClick={() => handleDelete(h.id)}
                              style={{ color: 'var(--danger)' }}><Trash2 size={14} /></button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* Sayfalama */}
      {toplamSayfa > 1 && (
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: 16, padding: '0 4px' }}>
          <span style={{ color: 'var(--gray-500)', fontSize: 13 }}>
            Toplam {toplamKayit} hasta • Sayfa {sayfa + 1} / {toplamSayfa}
          </span>
          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn btn-secondary" disabled={sayfa === 0} onClick={() => loadHastalar(sayfa - 1)}>← Önceki</button>
            <button className="btn btn-secondary" disabled={sayfa >= toplamSayfa - 1} onClick={() => loadHastalar(sayfa + 1)}>Sonraki →</button>
          </div>
        </div>
      )}

      {/* Hasta geçmişi modal (Doktor) */}
      {gecmisHasta && (
        <HastaGecmisiModal hasta={gecmisHasta} onClose={() => setGecmisHasta(null)} />
      )}

      {/* Hasta oluştur / düzenle modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>{editingHasta ? 'Hasta Düzenle' : 'Yeni Hasta'}</h3>
              <button className="btn-icon" onClick={() => setShowModal(false)}><X size={18} /></button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="modal-body">
                {error && <div style={{ background: 'var(--danger-bg)', color: '#991b1b', padding: '10px 14px', borderRadius: 6, fontSize: 13, marginBottom: 16 }}>{error}</div>}
                <div className="form-grid">
                  <div className="form-group"><label className="form-label">TC Kimlik *</label>
                    <input className="form-input" value={form.tcKimlik} maxLength={11}
                      onChange={e => setForm({ ...form, tcKimlik: e.target.value })} required disabled={!!editingHasta} /></div>
                  <div className="form-group"><label className="form-label">SGK Durumu</label>
                    <select className="form-select" value={form.sgkDurumu}
                      onChange={e => setForm({ ...form, sgkDurumu: e.target.value })}>
                      <option value="AKTIF">Aktif</option><option value="PASIF">Pasif</option></select></div>
                  <div className="form-group"><label className="form-label">Ad *</label>
                    <input className="form-input" value={form.ad}
                      onChange={e => setForm({ ...form, ad: e.target.value })} required /></div>
                  <div className="form-group"><label className="form-label">Soyad *</label>
                    <input className="form-input" value={form.soyad}
                      onChange={e => setForm({ ...form, soyad: e.target.value })} required /></div>
                  <div className="form-group"><label className="form-label">Doğum Tarihi</label>
                    <input className="form-input" type="date" value={form.dogumTarihi}
                      onChange={e => setForm({ ...form, dogumTarihi: e.target.value })} /></div>
                  <div className="form-group"><label className="form-label">Telefon</label>
                    <input className="form-input" value={form.telefon}
                      onChange={e => setForm({ ...form, telefon: e.target.value })} /></div>
                </div>
                <div className="form-group"><label className="form-label">Adres</label>
                  <textarea className="form-textarea" value={form.adres} rows={2}
                    onChange={e => setForm({ ...form, adres: e.target.value })} /></div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>İptal</button>
                <button type="submit" className="btn btn-primary">{editingHasta ? 'Güncelle' : 'Kaydet'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
