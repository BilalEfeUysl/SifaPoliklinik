import { useState, useEffect } from 'react';
import { yoneticiAPI } from '../api/axiosConfig';
import { useAuth } from '../context/AuthContext';
import { Building2, Users, Stethoscope, Plus, X, Pencil, Trash2 } from 'lucide-react';

const ROL_ETIKETLERI = {
  KAYIT_GOREVLISI: 'Kayıt Görevlisi',
  RANDEVU_GOREVLISI: 'Randevu Görevlisi',
  VEZNEDAR: 'Veznedar',
  YONETICI: 'Yönetici',
};

export default function YoneticiPage() {
  const { user } = useAuth();
  const [stats, setStats] = useState({ toplamKlinik: 0, toplamDoktor: 0, toplamKullanici: 0 });
  const [klinikler, setKlinikler] = useState([]);
  const [doktorlar, setDoktorlar] = useState([]);
  const [kullanicilar, setKullanicilar] = useState([]);
  const [loading, setLoading] = useState(true);

  // Klinik modals
  const [showKlinikModal, setShowKlinikModal] = useState(false);
  const [editKlinik, setEditKlinik] = useState(null);
  const [klinikForm, setKlinikForm] = useState({ ad: '', aciklama: '', muayeneUcreti: '' });

  // Doktor modals
  const [showDoktorModal, setShowDoktorModal] = useState(false);
  const [editDoktor, setEditDoktor] = useState(null);
  const [doktorForm, setDoktorForm] = useState({ ad: '', soyad: '', email: '', sifre: '', telefon: '', unvan: '', uzmanlikAlani: '', klinikId: '' });

  // Görevli modals
  const [showGorevliModal, setShowGorevliModal] = useState(false);
  const [editGorevli, setEditGorevli] = useState(null);
  const [gorevliForm, setGorevliForm] = useState({ ad: '', soyad: '', email: '', sifre: '', telefon: '', rol: 'KAYIT_GOREVLISI' });

  useEffect(() => { loadData(); }, []);

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

  // ---- Klinik handlers ----

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

  const handleKlinikSil = async (id) => {
    if (!window.confirm('Bu kliniği silmek istediğinizden emin misiniz?')) return;
    try {
      await yoneticiAPI.deleteKlinik(id);
      loadData();
    } catch (err) {
      alert(err.response?.data?.mesaj || 'Klinik silinemedi.');
    }
  };

  const openKlinikDuzenle = (k) => {
    setEditKlinik(k);
    setKlinikForm({ ad: k.ad, aciklama: k.aciklama || '', muayeneUcreti: k.muayeneUcreti });
    setShowKlinikModal(true);
  };

  const closeKlinikModal = () => {
    setShowKlinikModal(false);
    setEditKlinik(null);
  };

  // ---- Doktor handlers ----

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

  const handleDoktorSil = async (id) => {
    if (!window.confirm('Bu doktoru silmek istediğinizden emin misiniz? Bağlı tüm randevular da silinecektir.')) return;
    try {
      await yoneticiAPI.deleteDoktor(id);
      loadData();
    } catch (err) {
      alert(err.response?.data?.mesaj || 'Doktor silinemedi.');
    }
  };

  const openDoktorDuzenle = (d) => {
    setEditDoktor(d);
    setDoktorForm({ ad: '', soyad: '', email: '', sifre: '', telefon: '', unvan: d.unvan, uzmanlikAlani: d.uzmanlikAlani, klinikId: String(d.klinik?.id) });
    setShowDoktorModal(true);
  };

  const closeDoktorModal = () => {
    setShowDoktorModal(false);
    setEditDoktor(null);
  };

  const handleDoktorMusaitlikToggle = async (id) => {
    try {
      await yoneticiAPI.toggleDoktorMusaitlik(id);
      loadData();
    } catch (err) { console.error(err); }
  };

  // ---- Görevli handlers ----

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

  const handleGorevliSil = async (id) => {
    if (!window.confirm('Bu görevliyi silmek istediğinizden emin misiniz?')) return;
    try {
      await yoneticiAPI.deleteGorevli(id);
      loadData();
    } catch (err) {
      alert(err.response?.data?.mesaj || 'Görevli silinemedi.');
    }
  };

  const closeGorevliModal = () => {
    setShowGorevliModal(false);
    setEditGorevli(null);
  };

  if (loading) return <div className="empty-state"><p>Yükleniyor...</p></div>;

  return (
    <div>
      <div className="page-header">
        <h2>Yönetici Paneli</h2>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon green"><Building2 size={22} /></div>
          <div className="stat-content">
            <h4>Klinikler</h4>
            <div className="value">{stats.toplamKlinik || 0}</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon purple"><Stethoscope size={22} /></div>
          <div className="stat-content">
            <h4>Doktorlar</h4>
            <div className="value">{stats.toplamDoktor || 0}</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon blue"><Users size={22} /></div>
          <div className="stat-content">
            <h4>Sistem Kullanıcıları</h4>
            <div className="value">{stats.toplamKullanici || 0}</div>
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
        {/* Klinikler */}
        <div className="card">
          <div className="card-header">
            <h3>Klinikler</h3>
            <button className="btn btn-sm btn-primary" onClick={() => { setEditKlinik(null); setKlinikForm({ ad: '', aciklama: '', muayeneUcreti: '' }); setShowKlinikModal(true); }}>
              <Plus size={14} /> Yeni
            </button>
          </div>
          <div className="card-body" style={{ padding: 0 }}>
            <div className="data-table-wrapper"><table className="data-table"><thead><tr>
              <th>Klinik Adı</th><th>Ücret</th><th>İşlem</th>
            </tr></thead><tbody>
              {klinikler.map(k=>(
                <tr key={k.id}>
                  <td style={{fontWeight:600}}>{k.ad}</td>
                  <td>{k.muayeneUcreti} TL</td>
                  <td>
                    <div className="action-btn-group">
                      <button className="action-btn action-btn-edit" title="Düzenle" onClick={() => openKlinikDuzenle(k)}><Pencil /></button>
                      <button className="action-btn action-btn-delete" title="Sil" onClick={() => handleKlinikSil(k.id)}><Trash2 /></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody></table></div>
          </div>
        </div>

        {/* Doktorlar */}
        <div className="card">
          <div className="card-header">
            <h3>Doktorlar</h3>
            <button className="btn btn-sm btn-primary" onClick={() => { setEditDoktor(null); setDoktorForm({ ad: '', soyad: '', email: '', sifre: '', telefon: '', unvan: '', uzmanlikAlani: '', klinikId: '' }); setShowDoktorModal(true); }}>
              <Plus size={14} /> Yeni
            </button>
          </div>
          <div className="card-body" style={{ padding: 0 }}>
            <div className="data-table-wrapper"><table className="data-table"><thead><tr>
              <th>Doktor</th><th>Klinik</th><th>Durum</th><th>İşlem</th>
            </tr></thead><tbody>
              {doktorlar.map(d=>(
                <tr key={d.id}>
                  <td style={{fontWeight:600}}>{d.unvan} {d.kullanici?.ad} {d.kullanici?.soyad}</td>
                  <td>{d.klinik?.ad}</td>
                  <td><span className={`badge ${d.musaitMi !== false ? 'badge-success' : 'badge-danger'}`}>
                    {d.musaitMi !== false ? 'Müsait' : 'Müsait Değil'}
                  </span></td>
                  <td>
                    <div style={{display:'flex',alignItems:'center',gap:6}}>
                      <button className="btn btn-sm btn-secondary" style={{fontSize:11,padding:'4px 10px'}}
                        onClick={() => handleDoktorMusaitlikToggle(d.id)}>
                        {d.musaitMi !== false ? 'Pasife Al' : 'Aktife Al'}
                      </button>
                      <div className="action-btn-group">
                        <button className="action-btn action-btn-edit" title="Düzenle" onClick={() => openDoktorDuzenle(d)}><Pencil /></button>
                        <button className="action-btn action-btn-delete" title="Sil" onClick={() => handleDoktorSil(d.id)}><Trash2 /></button>
                      </div>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody></table></div>
          </div>
        </div>
      </div>

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
                <td>
                  <div className="action-btn-group">
                    <button className="action-btn action-btn-edit" title="Düzenle" onClick={() => {
                      setEditGorevli(u);
                      setGorevliForm({ ad: u.ad, soyad: u.soyad, email: u.email, sifre: '', telefon: u.telefon || '', rol: u.rol });
                      setShowGorevliModal(true);
                    }}><Pencil /></button>
                    <button className="action-btn action-btn-delete" title="Sil" onClick={() => handleGorevliSil(u.id)}><Trash2 /></button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody></table></div>
        </div>
      </div>

      {/* Klinik Ekleme/Düzenleme Modal */}
      {showKlinikModal && (
        <div className="modal-overlay" onClick={closeKlinikModal}>
          <div className="modal" onClick={e=>e.stopPropagation()}>
            <div className="modal-header">
              <h3>{editKlinik ? 'Klinik Düzenle' : 'Yeni Klinik Ekle'}</h3>
              <button className="btn-icon" onClick={closeKlinikModal}><X size={18}/></button>
            </div>
            <form onSubmit={handleKlinikSubmit}>
              <div className="modal-body">
                <div className="form-group"><label className="form-label">Klinik Adı *</label>
                  <input className="form-input" value={klinikForm.ad} onChange={e=>setKlinikForm({...klinikForm, ad:e.target.value})} required/></div>
                <div className="form-group"><label className="form-label">Muayene Ücreti (TL) *</label>
                  <input className="form-input" type="number" step="0.01" value={klinikForm.muayeneUcreti} onChange={e=>setKlinikForm({...klinikForm, muayeneUcreti:e.target.value})} required/></div>
                <div className="form-group"><label className="form-label">Açıklama</label>
                  <textarea className="form-textarea" value={klinikForm.aciklama} rows={2} onChange={e=>setKlinikForm({...klinikForm, aciklama:e.target.value})}/></div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={closeKlinikModal}>İptal</button>
                <button type="submit" className="btn btn-primary">Kaydet</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Doktor Ekleme/Düzenleme Modal */}
      {showDoktorModal && (
        <div className="modal-overlay" onClick={closeDoktorModal}>
          <div className="modal" onClick={e=>e.stopPropagation()}>
            <div className="modal-header">
              <h3>{editDoktor ? 'Doktor Düzenle' : 'Yeni Doktor Ekle'}</h3>
              <button className="btn-icon" onClick={closeDoktorModal}><X size={18}/></button>
            </div>
            <form onSubmit={handleDoktorSubmit}>
              <div className="modal-body">
                <div className="form-grid">
                  {!editDoktor && <>
                    <div className="form-group"><label className="form-label">Ad *</label>
                      <input className="form-input" value={doktorForm.ad} onChange={e=>setDoktorForm({...doktorForm, ad:e.target.value})} required/></div>
                    <div className="form-group"><label className="form-label">Soyad *</label>
                      <input className="form-input" value={doktorForm.soyad} onChange={e=>setDoktorForm({...doktorForm, soyad:e.target.value})} required/></div>
                    <div className="form-group"><label className="form-label">Email (Giriş için) *</label>
                      <input className="form-input" type="email" value={doktorForm.email} onChange={e=>setDoktorForm({...doktorForm, email:e.target.value})} required/></div>
                  </>}
                  <div className="form-group"><label className="form-label">{editDoktor ? 'Yeni Şifre (boş bırakılabilir)' : 'Şifre *'}</label>
                    <input className="form-input" type="password" value={doktorForm.sifre} onChange={e=>setDoktorForm({...doktorForm, sifre:e.target.value})} {...(!editDoktor && {required:true})}/></div>
                  <div className="form-group"><label className="form-label">Ünvan (Örn: Uzm. Dr.) *</label>
                    <input className="form-input" value={doktorForm.unvan} onChange={e=>setDoktorForm({...doktorForm, unvan:e.target.value})} required/></div>
                  <div className="form-group"><label className="form-label">Uzmanlık Alanı *</label>
                    <input className="form-input" value={doktorForm.uzmanlikAlani} onChange={e=>setDoktorForm({...doktorForm, uzmanlikAlani:e.target.value})} required/></div>
                  <div className="form-group" style={{gridColumn: '1 / -1'}}><label className="form-label">Klinik *</label>
                    <select className="form-select" value={doktorForm.klinikId} onChange={e=>setDoktorForm({...doktorForm, klinikId:e.target.value})} required>
                      <option value="">Seçiniz...</option>
                      {klinikler.map(k=>(<option key={k.id} value={k.id}>{k.ad}</option>))}
                    </select></div>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={closeDoktorModal}>İptal</button>
                <button type="submit" className="btn btn-primary">Kaydet</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Görevli Ekleme/Düzenleme Modal */}
      {showGorevliModal && (
        <div className="modal-overlay" onClick={closeGorevliModal}>
          <div className="modal" onClick={e=>e.stopPropagation()}>
            <div className="modal-header">
              <h3>{editGorevli ? 'Görevli Düzenle' : 'Yeni Görevli Ekle'}</h3>
              <button className="btn-icon" onClick={closeGorevliModal}><X size={18}/></button>
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
                <button type="button" className="btn btn-secondary" onClick={closeGorevliModal}>İptal</button>
                <button type="submit" className="btn btn-primary">Kaydet</button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
}
