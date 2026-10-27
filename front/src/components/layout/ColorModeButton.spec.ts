import { beforeEach, describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { createVuetify } from 'vuetify';
import * as components from 'vuetify/components';
import ColorModeButton from './ColorModeButton.vue';
import { themes } from '@/plugins/themes';
import { i18n } from '@/test/setup';

function mountButton(defaultTheme: string) {
  const vuetify = createVuetify({ components, theme: { defaultTheme, themes } });
  const wrapper = mount(ColorModeButton, { global: { plugins: [vuetify, i18n] } });
  return { wrapper, vuetify };
}

describe('ColorModeButton.vue', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it.each([
    { defaultTheme: 'flexstoreLight', label: 'Dark mode' },
    { defaultTheme: 'flexstoreDark', label: 'Light mode' },
  ])('in the theme $defaultTheme, the button offers the "$label"', ({ defaultTheme, label }) => {
    // Act
    const { wrapper } = mountButton(defaultTheme);
    // Assert
    expect(wrapper.find('button').attributes('aria-label')).toBe(label);
  });

  it('a click switches the light theme to the dark one and remembers it', async () => {
    // Arrange
    const { wrapper, vuetify } = mountButton('flexstoreLight');
    // Act
    await wrapper.find('button').trigger('click');
    // Assert
    expect(vuetify.theme.global.name.value).toBe('flexstoreDark');
    expect(localStorage.getItem('flexstore.colorMode')).toBe('dark');
  });

  it('a click switches the dark theme back to the light one', async () => {
    // Arrange
    const { wrapper, vuetify } = mountButton('flexstoreDark');
    // Act
    await wrapper.find('button').trigger('click');
    // Assert
    expect(vuetify.theme.global.name.value).toBe('flexstoreLight');
  });
});
