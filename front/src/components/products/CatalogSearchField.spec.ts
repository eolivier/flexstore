import { describe, expect, it } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import CatalogSearchField from './CatalogSearchField.vue';
import { vuetify, i18n } from '@/test/setup';

async function mountField(path: string) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
  });
  await router.push(path);
  const wrapper = mount(CatalogSearchField, { global: { plugins: [vuetify, i18n, router] } });
  return { wrapper, router };
}

describe('CatalogSearchField.vue', () => {
  it('a search opens the catalog filtered by the text', async () => {
    // Arrange
    const { wrapper, router } = await mountField('/');
    await wrapper.find('input').setValue('  jacket ');
    // Act
    await wrapper.find('input').trigger('keydown', { key: 'Enter' });
    await flushPromises();
    // Assert
    expect(router.currentRoute.value.fullPath).toBe('/products/?q=jacket');
  });

  it('the field shows the text searched on the catalog', async () => {
    // Act
    const { wrapper } = await mountField('/products/?q=lamp');
    // Assert
    expect((wrapper.find('input').element as HTMLInputElement).value).toBe('lamp');
  });
});
