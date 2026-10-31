import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import DiscountCodeField from './DiscountCodeField.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

function mountField() {
  return mount(DiscountCodeField, { props: { modelValue: null }, global: { plugins: [vuetify, i18n] } });
}

async function apply(wrapper: ReturnType<typeof mountField>, code: string) {
  await wrapper.find('input').setValue(code);
  await wrapper.find('button').trigger('click');
  await flushPromises();
}

describe('DiscountCodeField.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('applies the code with what it takes off the cart', async () => {
    // Arrange
    const preview = { code: 'WELCOME10', amountOff: 7, currency: '€' };
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(preview) });
    const wrapper = mountField();
    // Act
    await apply(wrapper, 'welcome10');
    // Assert
    const [url] = mockFetch.mock.calls[0];
    expect(url).toMatch(/\/api\/orders\/discount-preview\?code=welcome10$/);
    expect(wrapper.emitted('update:modelValue')).toEqual([[preview]]);
  });

  it.each([
    { status: 404, message: 'This code does not exist.' },
    { status: 410, message: 'This code has expired or is used up.' },
  ])('says why a code is refused ($status)', async ({ status, message }) => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status });
    const wrapper = mountField();
    // Act
    await apply(wrapper, 'NOPE');
    // Assert
    expect(wrapper.text()).toContain(message);
    expect(wrapper.emitted('update:modelValue')).toBeUndefined();
  });
});
