import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import AdminDiscountCodes from './AdminDiscountCodes.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

const welcome = {
  code: 'WELCOME10',
  reductionType: 'PERCENTAGE',
  reductionValue: 10,
  expiresAt: '2026-12-31T22:59:59Z',
  usageLimit: 100,
  timesUsed: 3,
};

async function mountPage() {
  const wrapper = mount(AdminDiscountCodes, { global: { plugins: [vuetify, i18n, router] } });
  await flushPromises();
  return wrapper;
}

describe('AdminDiscountCodes.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('lists the discount codes with their reduction and their uses', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve([welcome]) });
    // Act
    const wrapper = await mountPage();
    // Assert
    expect(wrapper.text()).toContain('WELCOME10');
    expect(wrapper.text()).toContain('10 %');
    expect(wrapper.text()).toContain('3 / 100');
  });

  it('gives a new discount code, in upper case', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 201, json: () => Promise.resolve([]) });
    const wrapper = await mountPage();
    await wrapper.find('.code input').setValue('summer20');
    await wrapper.find('input[type="date"]').setValue('2026-12-31');
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();
    // Assert
    const [, init] = mockFetch.mock.calls.find(([, init]) => init?.method === 'POST')!;
    expect(JSON.parse(init.body)).toMatchObject({
      code: 'summer20',
      reductionType: 'PERCENTAGE',
      usageLimit: 100,
    });
  });
});
