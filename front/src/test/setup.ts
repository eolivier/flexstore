import { config } from '@vue/test-utils';
import { createVuetify } from 'vuetify';
import * as components from 'vuetify/components';
import * as directives from 'vuetify/directives';
import { vi } from 'vitest';
import { createI18n } from 'vue-i18n';
import messages from '@/i18n/messages';

// Create vuetify instance for tests
export const vuetify = createVuetify({
  components,
  directives,
});

// Create i18n instance for tests
export const i18n = createI18n({
  locale: 'en',
  messages,
  legacy: false,
});

// Global plugins for all tests
config.global.plugins = [vuetify, i18n];

// Mock import.meta.env
vi.stubEnv('VITE_API_URL', 'http://localhost:8080');
