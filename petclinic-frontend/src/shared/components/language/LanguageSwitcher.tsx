import { useTranslation } from 'react-i18next';

export function LanguageSwitcher(): JSX.Element {
  const { i18n } = useTranslation();

  const changeLanguage = (lng: string): void => {
    i18n.changeLanguage(lng);
  };

  //This next line will check if the active language is fr or en
  const currentLang = i18n.language || 'en';

  return (
    <div className="flex items-center gap-2">
      <button
        onClick={() => changeLanguage('en')}
        className={`px-2 py-1 rounded test-sm font-medium transition ${
          currentLang.startsWith('en')
            ? 'bg-blue-600 text-white'
            : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
        }`}
      >
        EN
      </button>

      <button
        onClick={() => changeLanguage('fr')}
        className={`px-2 py-1 rounded test-sm font-medium transition ${
          currentLang.startsWith('fr')
            ? 'bg-blue-600 text-white'
            : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
        }`}
      >
        Fr
      </button>
    </div>
  );
}
