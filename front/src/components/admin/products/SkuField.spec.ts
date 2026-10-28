import { describe, expect, it } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import SkuField from './SkuField.vue';
import { vuetify, i18n } from '@/test/setup';

function mountSkuField(sku: string, alreadyUsed = false) {
  return mount(SkuField, {
    props: { modelValue: sku, alreadyUsed },
    global: { plugins: [vuetify, i18n] },
  });
}

describe('SkuField.vue', () => {
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
});
