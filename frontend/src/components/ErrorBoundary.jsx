import { Component } from 'react';
import { AlertTriangle } from 'lucide-react';

export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, info) {
    console.error('Uygulama hatası:', error, info);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{
          display: 'flex', flexDirection: 'column', alignItems: 'center',
          justifyContent: 'center', height: '100vh', gap: 16, color: '#991b1b'
        }}>
          <AlertTriangle size={48} />
          <h2>Bir hata oluştu</h2>
          <p style={{ color: '#6b7280', fontSize: 14 }}>
            {this.state.error?.message || 'Beklenmedik bir hata meydana geldi.'}
          </p>
          <button
            style={{ padding: '8px 16px', background: '#0d9488', color: 'white', border: 'none', borderRadius: 6, cursor: 'pointer' }}
            onClick={() => { this.setState({ hasError: false, error: null }); window.location.href = '/'; }}
          >
            Ana Sayfaya Dön
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}
