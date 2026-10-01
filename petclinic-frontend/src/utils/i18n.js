import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import HttpApi from 'i18next-http-backend';

void i18n
  .use(HttpApi)
  .use(initReactI18next) // passes i18n down to react-i18next
  .init({
    fallbackLng: 'en', // use en if detected lng is not available
    lng: 'en', // language to use, more information here: https://www.i18next.com/overview/configuration-options#languages-namespaces-resources
    // you can use the i18n.changeLanguage function to change the language manually: https://www.i18next.com/overview/api#changelanguage
    // if you're using a language detector, do not define the lng option
    ns: [
      'bills',
      'carts',
      'customers',
      'faq',
      'home',
      'inventories',
      'products',
      'promos',
      'users',
      'veterinarians',
      'visits',
    ],
    defaultNS: 'customers',

    backend: {
      loadPath: '/locales/{{lng}}/{{ns}}.json',
    },
    interpolation: {
      escapeValue: false,
    },
    react: {
      useSuspense: false, // Prevents blank screen when Suspense is omitted
    },
  })
  .catch(err => console.error('Failed to initialize i18n:', err));

export default i18n;
