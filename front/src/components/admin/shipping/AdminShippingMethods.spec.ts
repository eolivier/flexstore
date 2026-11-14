import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import AdminShippingMethods from './AdminShippingMethods.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

const standard = {
  id: 'STANDARD',
  label: 'Livraison standard',
  cost: 4.9,
  freeShippingEligibility: 'ELIGIBLE',
  status: 'ENABLED',
};
const express = {
  id: 'EXPRESS',
  label: 'Livraison express',
  cost: 9.9,
  freeShippingEligibility: 'NOT_ELIGIBLE',
  status: 'ENABLED',
};

async function mountPage() {
  const wrapper = mount(AdminShippingMethods, { global: { plugins: [vuetify, i18n, router] } });
  await flushPromises();
  return wrapper;
}

describe('AdminShippingMethods.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('should list the shipping methods with their cost', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve([standard, express]) });
    // Act
    const wrapper = await mountPage();
    // Assert
    expect(wrapper.text()).toContain('Livraison standard');
    expect(wrapper.text()).toContain('Livraison express');
  });

  it('should add a shipping method with its name and its cost', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 201, json: () => Promise.resolve([standard]) });
    const wrapper = await mountPage();
    await wrapper.find('input[type="text"]').setValue('Point relais');
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();
    // Assert
    const [, init] = mockFetch.mock.calls.find(([, init]) => init?.method === 'POST')!;
    expect(JSON.parse(init.body)).toMatchObject({ label: 'Point relais', cost: 4.9 });
  });

  it('should tell that at least one shipping method must stay offered', async () => {
    // Arrange
    mockFetch.mockImplementation((_url: string, init?: { method?: string }) =>
      Promise.resolve(
        init?.method === 'PUT'
          ? { ok: false, status: 409, json: () => Promise.resolve({}) }
          : { ok: true, status: 200, json: () => Promise.resolve([standard]) }
      )
    );
    const wrapper = await mountPage();
    // Act
    await wrapper.find('input[type="checkbox"]').setValue(false);
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain(i18n.global.t('shippingMethods.errors.lastOne'));
  });
});
