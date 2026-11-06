import { beforeEach, describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia } from 'pinia';
import { createRouter, createMemoryHistory } from 'vue-router';
import { createVuetify } from 'vuetify';
import * as components from 'vuetify/components';
import MyPreferences from './MyPreferences.vue';
import { themes } from '@/plugins/themes';
import { i18n } from '@/test/setup';

function mountPreferences(defaultTheme: string) {
  const vuetify = createVuetify({ components, theme: { defaultTheme, themes } });
  const stubs = { AccountBreadcrumbs: true };
  const router = createRouter({ history: createMemoryHistory(), routes: [{ path: '/:any(.*)', component: {} }] });
  const plugins = [vuetify, i18n, createPinia(), router];
  const wrapper = mount(MyPreferences, { global: { plugins, stubs } });
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

  it('choosing a language switches the shop to it', async () => {
    // Arrange
    i18n.global.locale.value = 'en';
    const { wrapper } = mountPreferences('flexstoreLight');
    // Act
    wrapper.findComponent({ name: 'VSelect' }).vm.$emit('update:modelValue', 'fr');
    await wrapper.vm.$nextTick();
    // Assert
    expect(i18n.global.locale.value).toBe('fr');
    i18n.global.locale.value = 'en';
  });
});
