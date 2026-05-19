import { useState, useEffect } from 'react';
import { odemeAPI } from '../api/axiosConfig';
import { useAuth } from '../context/AuthContext';
import { CreditCard, CheckCircle, XCircle } from 'lucide-react';

export default function OdemePage() {
  const { hasRole } = useAuth();
  const [odemeler, setOdemeler] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [tahsilatModal, setTahsilatModal] = useState(null);
  const [sgkYukleniyor, setSgkYukleniyor] = useState(null);
  const [sgkMesaj, setSgkMesaj] = useState('');

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const res = await odemeAPI.getAll();
      setOdemeler(res.data);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const handleOdemeOlustur = async (muayeneId) => {
    setError('');
    try {
      await odemeAPI.create(muayeneId);
      loadData();
    } catch (err) {
      setError(err.response?.data?.mesaj || 'Ödeme oluşturulamadı.');
    }
  };

  const openTahsilatModal = (id) => setTahsilatModal({ id });

  const handleTahsilat = async (odemeTipi) => {
    if (!tahsilatModal) return;
    setError('');
    try {
      const res = await odemeAPI.tahsilat(tahsilatModal.id, odemeTipi);
      console.log('[TAHSILAT RESPONSE]', res.data);
      setTahsilatModal(null);
      setOdemeler(prev => prev.map(o => o.id === res.data.id ? res.data : o));
      await loadData();
    } catch (err) { setError(err.response?.data?.mesaj || 'Tahsilat başarısız.'); }
  };

  const handleSgkSorgula = async (o) => {
    setSgkYukleniyor(o.id);
    setSgkMesaj('');
    try {
      const res = await odemeAPI.sgkSorgula(o.id);
      const indirim = res.data.sgkIndirimi;
      setSgkMesaj(indirim > 0
        ? `SGK aktif — %80 indirim uygulandı. Net tutar: ${res.data.netTutar} TL`
        : 'SGK pasif — indirim uygulanmadı.'
      );
      loadData();
    } catch (err) {
      const status = err.response?.status;
      const msg = err.response?.data?.mesaj || err.response?.data?.message || err.message || 'Bilinmeyen hata';
      setSgkMesaj(`SGK sorgu başarısız${status ? ` (${status})` : ''}: ${msg}`);
    } finally {
      setSgkYukleniyor(null);
    }
  };

  const handleIptal = async (id) => {
    setError('');
    if (!confirm('Bu ödemeyi iptal etmek istediğinize emin misiniz?')) return;
    try {
      await odemeAPI.cancel(id);
      loadData();
    } catch (err) { setError(err.response?.data?.mesaj || 'İptal başarısız.'); }
  };

  if (loading) return <div className="empty-state"><p>Yükleniyor...</p></div>;

  return (
    <div>
      <div className="page-header">
        <h2>Ödeme İşlemleri</h2>
      </div>

      {error && (
        <div style={{background:'var(--danger-bg)',color:'#991b1b',padding:'10px 14px',borderRadius:6,fontSize:13,marginBottom:16,display:'flex',alignItems:'center',gap:8}}>
          <span>{error}</span>
          <button onClick={()=>setError('')} style={{marginLeft:'auto',background:'none',border:'none',cursor:'pointer',color:'inherit'}}>✕</button>
        </div>
      )}
      {sgkMesaj && (
        <div style={{ background: '#f0fdf4', border: '1px solid #86efac', color: '#166534', padding: '10px 14px', borderRadius: 6, fontSize: 13, marginBottom: 16, display: 'flex', alignItems: 'center', gap: 8 }}>
          <span>{sgkMesaj}</span>
          <button onClick={() => setSgkMesaj('')} style={{ marginLeft: 'auto', background: 'none', border: 'none', cursor: 'pointer', color: 'inherit' }}>✕</button>
        </div>
      )}
      <div className="card">
        <div className="card-header">
          <h3>Ödemeler Listesi</h3>
        </div>
        <div className="card-body" style={{padding:0}}>
          {odemeler.length === 0 ? (
            <div className="empty-state"><CreditCard size={48}/><h4>Ödeme bulunamadı</h4></div>
          ) : (
            <div className="data-table-wrapper"><table className="data-table"><thead><tr>
              <th>ID</th><th>Hasta</th><th>TC Kimlik</th><th>Toplam Tutar</th><th>SGK Durumu</th><th>SGK İndirimi</th><th>Net Tutar</th><th>Ödeme Tipi</th><th>Durum</th><th>İşlem</th>
            </tr></thead><tbody>
              {odemeler.map(o=>(
                <tr key={o.id}>
                  <td>#{o.id}</td>
                  <td style={{fontWeight:600}}>{o.hasta?.ad} {o.hasta?.soyad}</td>
                  <td style={{fontFamily:'monospace'}}>{o.hasta?.tcKimlik}</td>
                  <td>{o.toplamTutar} TL</td>
                  <td>
                    {o.sgkAktif === true
                      ? <span style={{color:'#166534',background:'#dcfce7',padding:'2px 8px',borderRadius:99,fontSize:11,fontWeight:700}}>✓ Aktif</span>
                      : o.sgkAktif === false
                      ? <span style={{color:'#991b1b',background:'#fee2e2',padding:'2px 8px',borderRadius:99,fontSize:11,fontWeight:700}}>✗ Pasif</span>
                      : <span style={{color:'var(--gray-400)'}}>—</span>}
                  </td>
                  <td style={{color:'var(--success)'}}>{o.sgkIndirimi > 0 ? `-${o.sgkIndirimi} TL` : '-'}</td>
                  <td style={{fontWeight:800}}>{o.netTutar} TL</td>
                  <td>
                    {o.odemeTipi === 'NAKIT' ? '💵 Nakit' :
                     o.odemeTipi === 'KREDI_KARTI' ? '💳 Kredi Kartı' : '-'}
                  </td>
                  <td><span className={`badge ${o.odemeDurumu==='ODENDI'?'badge-success':o.odemeDurumu==='IPTAL'?'badge-danger':'badge-warning'}`}>
                    {o.odemeDurumu==='ODENDI'?'Ödendi':o.odemeDurumu==='IPTAL'?'İptal':'Bekliyor'}</span></td>
                  <td>
                    {o.odemeDurumu==='BEKLIYOR' && hasRole('VEZNEDAR') && (
                      <div style={{display:'flex',gap:6}}>
                        <button className="btn btn-sm btn-success" onClick={()=>openTahsilatModal(o.id)} style={{fontSize:11,padding:'4px 10px'}}>
                          <CheckCircle size={12}/> Tahsil Et</button>
                        <button className="btn btn-sm btn-danger" onClick={()=>handleIptal(o.id)} style={{fontSize:11,padding:'4px 10px'}}>
                          <XCircle size={12}/> İptal</button>
                        <button
                          className="btn btn-sm"
                          onClick={() => handleSgkSorgula(o)}
                          disabled={sgkYukleniyor === o.id}
                          style={{ fontSize: 11, padding: '4px 10px', background: '#e0f2fe', color: '#0369a1', border: '1px solid #bae6fd' }}
                        >
                          {sgkYukleniyor === o.id ? '...' : '🔄 SGK'}
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody></table></div>
          )}
        </div>
      </div>
      
      {tahsilatModal && (
        <div className="modal-overlay" onClick={() => setTahsilatModal(null)}>
          <div className="modal" style={{ maxWidth: 360 }} onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3 style={{ margin: 0, fontSize: 15 }}>Ödeme Yöntemi Seçin</h3>
              <button className="btn-icon" onClick={() => setTahsilatModal(null)}>✕</button>
            </div>
            <div className="modal-body" style={{ display: 'flex', gap: 12 }}>
              <button
                className="btn btn-primary"
                style={{ flex: 1, padding: '14px 0', fontSize: 14, fontWeight: 700 }}
                onClick={() => handleTahsilat('NAKIT')}
              >
                💵 Nakit
              </button>
              <button
                className="btn btn-secondary"
                style={{ flex: 1, padding: '14px 0', fontSize: 14, fontWeight: 700 }}
                onClick={() => handleTahsilat('KREDI_KARTI')}
              >
                💳 Kredi Kartı
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
