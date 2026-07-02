import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import Btn from '../components/Btn';
import { t } from '../i18n';

function Direcciones() {
  const navigate = useNavigate();
  const { user, addresses } = useAuth();

  return (
    <main className="screen">
      <div className="eyebrow mono">{t('info.addresses.eyebrow', { name: user.first_name.toUpperCase() })}</div>
      <h1 className="screen-title">{t('info.addresses.title')} <em>{t('info.addresses.titleEm')}</em></h1>

      <div className="addr-grid">
        {addresses.map((a) => (
          <div key={a.address_id} className="addr-card">
            <div className="addr-card-head">
              <div className="eyebrow mono">DIRECCIÓN #{a.address_id}</div>
              <span className="addr-card-pill">activa</span>
            </div>
            <div className="addr-card-street">{a.street}</div>
            <div className="addr-card-city">{a.city} · {a.state} · CP {a.zip_code}</div>
            {a.reference_note && <div className="mono addr-card-note">{a.reference_note}</div>}
          </div>
        ))}

        <button className="addr-card-add mono" onClick={() => navigate('/checkout')}>
          {t('checkout.address.addNew')}
        </button>
      </div>

      <div className="addr-back">
        <Btn variant="ghost" onClick={() => navigate('/pedidos')}>← Volver a mis pedidos</Btn>
      </div>
    </main>
  );
}

export default Direcciones;
