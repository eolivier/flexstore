import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import SkuField from './SkuField.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

function mountSkuField(sku: string, alreadyUsed = false) {
  return mount(SkuField, {
    props: { modelValue: sku, alreadyUsed, category: 'CLOTHING', name: 'Organic Cotton T-shirt' },
    global: { plugins: [vuetify, i18n] },
  });
}

describe('SkuField.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('explains what a SKU is in a tooltip', async () => {
    // Arrange
    const wrapper = mountSkuField('CLO-1');
    const help = wrapper.find('[aria-label="What is a SKU?"]');
    // Act
    await help.trigger('mouseenter');
    await flushPromises();
    // Assert
    expect(document.body.textContent).toContain('Stock Keeping Unit: your own reference for this product');
  });

  it('refuses a malformed SKU', async () => {
    // Arrange
    const wrapper = mountSkuField('');
    // Act
    await wrapper.find('input').setValue('T SHIRT');
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain('Upper-case letters and digits, in groups separated by hyphens');
  });

  it('says that the SKU is used by another product', () => {
    // Act
    const wrapper = mountSkuField('CLO-1', true);
    // Assert
    expect(wrapper.text()).toContain('This SKU is already used by another product');
  });

  it('fills the field with the SKU proposed from the category and the name', async () => {
    // Arrange
    const proposal = { sku: 'CLO-ORGANIC-COTTON-0001' };
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(proposal) });
    const wrapper = mountSkuField('');
    // Act
    await wrapper.find('.generate').trigger('click');
    await flushPromises();
    // Assert
    const [url] = mockFetch.mock.calls[0];
    expect(url).toMatch(
      /\/api\/admin\/products\/sku-proposal\?category=CLOTHING&name=Organic\+Cotton\+T-shirt$/
    );
    expect(wrapper.emitted('update:modelValue')).toEqual([['CLO-ORGANIC-COTTON-0001']]);
  });

  it('says that no SKU could be generated', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 500 });
    const wrapper = mountSkuField('');
    // Act
    await wrapper.find('.generate').trigger('click');
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain('No SKU could be generated, try again');
    expect(wrapper.emitted('update:modelValue')).toBeUndefined();
  });
});
