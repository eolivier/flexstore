import { createI18n } from 'vue-i18n';
import { en as vuetifyEn, fr as vuetifyFr } from 'vuetify/locale';
import messages from '../i18n/messages';

// Vuetify's own texts (table footer, empty states, …) live under $vuetify so they follow the app locale
export default createI18n({
  legacy: false,
  locale: 'fr',
  fallbackLocale: 'en',
  messages: {
    en: { ...messages.en, $vuetify: vuetifyEn },
    fr: { ...messages.fr, $vuetify: vuetifyFr },
  },
});
