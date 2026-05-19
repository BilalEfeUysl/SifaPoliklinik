import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Lock, Mail } from 'lucide-react';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [sifre, setSifre] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, sifre);
    } catch (err) {
      setError(err.response?.data?.mesaj || 'Giriş başarısız. Email veya şifre hatalı.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-logo">
          <div className="login-logo-icon">Ş</div>
          <h1>Şifa Polikliniği</h1>
          <p>Bilgi Sistemi'ne Giriş Yapın</p>
        </div>

        {error && <div className="login-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">
              <Mail size={14} style={{ marginRight: 6, verticalAlign: 'middle' }} />
              Email Adresi
            </label>
            <input
              id="login-email"
              type="email"
              className="form-input"
              placeholder="ornek@sifa.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">
              <Lock size={14} style={{ marginRight: 6, verticalAlign: 'middle' }} />
              Şifre
            </label>
            <input
              id="login-password"
              type="password"
              className="form-input"
              placeholder="••••••••"
              value={sifre}
              onChange={(e) => setSifre(e.target.value)}
              required
            />
          </div>

          <button
            id="login-submit"
            type="submit"
            className="btn btn-primary login-btn"
            disabled={loading}
          >
            {loading ? 'Giriş yapılıyor...' : 'Giriş Yap'}
          </button>
        </form>

        <div style={{ marginTop: 24, textAlign: 'center' }}>
          <p style={{ fontSize: 11, color: 'var(--gray-500)' }}>
            Demo Giriş Bilgileri
          </p>
          <div style={{ fontSize: 11, color: 'var(--gray-500)', marginTop: 8, lineHeight: 1.8 }}>
            <div><strong style={{color:'var(--gray-400)'}}>Yönetici:</strong> admin@sifa.com / admin123</div>
            <div><strong style={{color:'var(--gray-400)'}}>Kayıt Görevlisi:</strong> ayse@sifa.com / kayit123</div>
            <div><strong style={{color:'var(--gray-400)'}}>Randevu:</strong> fatma@sifa.com / randevu123</div>
            <div><strong style={{color:'var(--gray-400)'}}>Veznedar:</strong> mehmet@sifa.com / vezne123</div>
            <div><strong style={{color:'var(--gray-400)'}}>Doktor:</strong> ahmet.dr@sifa.com / doktor123</div>
          </div>
        </div>
      </div>
    </div>
  );
}
