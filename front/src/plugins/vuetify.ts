import { createVuetify } from 'vuetify';
import * as components from 'vuetify/components';
import * as directives from 'vuetify/directives';
import { createVueI18nAdapter } from 'vuetify/locale/adapters/vue-i18n';
import { useI18n } from 'vue-i18n';
import i18n from './i18n';
import { themes } from './themes';
import { initialColorMode, themeNames } from '@/utils/colorMode';

import 'vuetify/styles';
import '@mdi/font/css/materialdesignicons.css';

export default createVuetify({
  components,
  directives,
  locale: {
    adapter: createVueI18nAdapter({ i18n, useI18n }),
  },
  theme: {
    // Chosen before the first render, so that a dark page does not flash light
    defaultTheme: themeNames[initialColorMode()],
    themes,
  },
  defaults: {
    VBtn: { rounded: 'lg', class: 'text-none font-weight-medium', elevation: 0 },
    VCard: { rounded: 'xl', elevation: 0, border: true },
    VTextField: { variant: 'outlined', density: 'comfortable', rounded: 'lg' },
    VSelect: { variant: 'outlined', density: 'comfortable', rounded: 'lg' },
    VChip: { rounded: 'lg' },
    VDataTable: { class: 'rounded-xl border' },
  },
});
