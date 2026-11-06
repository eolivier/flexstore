import { beforeEach, describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia } from 'pinia';
import { createVuetify } from 'vuetify';
import * as components from 'vuetify/components';
import MyPreferences from './MyPreferences.vue';
import { themes } from '@/plugins/themes';
import { i18n } from '@/test/setup';

function mountPreferences(defaultTheme: string) {
  const vuetify = createVuetify({ components, theme: { defaultTheme, themes } });
  const stubs = { AccountBreadcrumbs: true };
  const wrapper = mount(MyPreferences, { global: { plugins: [vuetify, i18n, createPinia()], stubs } });
  return { wrapper, vuetify };
}

describe('MyPreferences.vue', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it.each([
    { defaultTheme: 'flexstoreDark', checked: true },
    { defaultTheme: 'flexstoreLight', checked: false },
  ])('in the theme $defaultTheme, the dark mode switch is checked: $checked', ({ defaultTheme, checked }) => {
    // Act
    const { wrapper } = mountPreferences(defaultTheme);
    // Assert
    const input = wrapper.find<HTMLInputElement>('input[type="checkbox"]');
    expect(input.element.checked).toBe(checked);
  });

  it('switching the dark mode on changes the theme to the dark one', async () => {
    // Arrange
    const { wrapper, vuetify } = mountPreferences('flexstoreLight');
    // Act
    await wrapper.find('input[type="checkbox"]').setValue(true);
    // Assert
    expect(vuetify.theme.global.name.value).toBe('flexstoreDark');
  });
});
