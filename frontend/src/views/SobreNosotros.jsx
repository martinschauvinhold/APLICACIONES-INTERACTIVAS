import { useNavigate } from 'react-router-dom';
import Btn from '../components/Btn';
import { t } from '../i18n';

function SobreNosotros() {
  const navigate = useNavigate();
  const stats = t('info.about.stats');

  return (
    <main className="screen">
      <div className="eyebrow mono">{t('info.about.eyebrow')}</div>
      <h1 className="screen-title">{t('info.about.title')} <em>{t('info.about.titleEm')}</em></h1>

      <div className="info-2col">
        <div>
          <h2 className="about-h">
            {t('info.about.headPre')} <em>{t('info.about.headEm')}</em>,<br />
            {t('info.about.headPost')} <em>{t('info.about.headEm')}</em>.
          </h2>
          <p className="info-lead">{t('info.about.p1')}</p>
          <p className="info-lead">{t('info.about.p2')}</p>
          <Btn variant="primary" onClick={() => navigate('/')}>{t('info.about.cta')}</Btn>
        </div>

        <div className="info-col">
          {stats.map((item) => (
            <div key={item.num} className="stat-card">
              <div className="stat-card-num">{item.num}</div>
              <div>
                <div className="stat-card-label">{item.label}</div>
                <div className="stat-card-desc">{item.desc}</div>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="about-cta">
        <div className="eyebrow mono about-cta-eyebrow">{t('info.about.teamEyebrow')}</div>
        <h2 className="about-cta-h">{t('info.about.teamTitle')}</h2>
        <p className="about-cta-text">{t('info.about.teamText')}</p>
      </div>
    </main>
  );
}

export default SobreNosotros;
