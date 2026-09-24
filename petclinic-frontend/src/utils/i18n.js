import i18n from "i18next";
import { initReactI18next } from "react-i18next";


// const resources = {
//     en: {
//         translation: translationEN,
//     },
//     fr: {
//         translation: translationFR,
//     },
// };
//
// i18n
//     .use(initReactI18next) // passes i18n down to react-i18next
//     .init({
//         resources,
//         fallbackLng:'en', // use en if detected lng is not available
//         lng: "en", // language to use, more information here: https://www.i18next.com/overview/configuration-options#languages-namespaces-resources
//         // you can use the i18n.changeLanguage function to change the language manually: https://www.i18next.com/overview/api#changelanguage
//         // if you're using a language detector, do not define the lng option
//
//         interpolation: {
//             escapeValue: false // react already safes from xss
//         }
//     });
i18n
    .use(HttpApi)
    .use(initReactI18next) // passes i18n down to react-i18next
    .init({
        fallbackLng: 'en',// use en if detected lng is not available
        lng: 'en',// language to use, more information here: https://www.i18next.com/overview/configuration-options#languages-namespaces-resources
        // you can use the i18n.changeLanguage function to change the language manually: https://www.i18next.com/overview/api#changelanguage
        // if you're using a language detector, do not define the lng option
        backend: {
            loadPath: '/locales/{{lng}}/translation.json',
        },
        interpolation: {
            escapeValue: false,
        },
    });

export default i18n;