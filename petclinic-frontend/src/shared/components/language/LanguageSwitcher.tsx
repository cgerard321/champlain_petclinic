import { useTranslation } from 'react-i18next';

export function LanguageSwitcher(): JSX.Element {
  const { i18n } = useTranslation();

  const changeLanguage = (lng: string): void => {
    i18n.changeLanguage(lng);
  };

  //This next line will check if the active language is fr or en
  const currentLang = i18n.language || 'en';

  return (
    <div className="d-flex align-items-center gap-2">
      <button
        type="button"
        onClick={() => changeLanguage('en')}
        className={`btn btn-sm ${
          currentLang.startsWith('en') ? 'btn-primary' : 'btn-outline-secondary'
        }`}
      >
        EN
      </button>

      <button
        type="button"
        onClick={() => changeLanguage('fr')}
        className={`btn btn-sm ${
          currentLang.startsWith('fr') ? 'btn-primary' : 'btn-outline-secondary'
        }`}
      >
        FR
      </button>
    </div>
  );
}
