import { createVuetify } from 'vuetify';
import * as components from 'vuetify/components';
import * as directives from 'vuetify/directives';

import 'vuetify/styles';
import '@mdi/font/css/materialdesignicons.css';

const flexstoreLight = {
  dark: false,
  colors: {
    background: '#F7F6F3',
    surface: '#FFFFFF',
    'surface-variant': '#EEECE7',
    'on-surface-variant': '#3A3F4B',
    primary: '#1F2A44',
    secondary: '#E4572E',
    accent: '#E4572E',
    info: '#2F6FDE',
    success: '#2E8B57',
    warning: '#D98E04',
    error: '#C62828',
  },
  variables: {
    'border-color': '#1F2A44',
    'border-opacity': 0.1,
    'high-emphasis-opacity': 0.92,
    'medium-emphasis-opacity': 0.64,
  },
};

export default createVuetify({
  components,
  directives,
  theme: {
    defaultTheme: 'flexstoreLight',
    themes: { flexstoreLight },
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
