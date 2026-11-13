import { beforeEach, describe, expect, it } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { flushPromises, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import ProductForm, { type ProductFormValues } from './ProductForm.vue';
import AlertThresholdField from './AlertThresholdField.vue';
import { emptyTexts } from '@/utils/productTranslations';
import { emptyPromotion } from '@/utils/promotion';
import { vuetify, i18n } from '@/test/setup';

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

function lamp(alertThreshold: string | null): ProductFormValues {
  const texts = emptyTexts();
  texts.en = { name: 'Desk lamp', description: 'Warm light' };
  return {
    sku: 'LAMP-DESK',
    texts,
    category: 'CLOTHING',
    price: '35',
    vatRate: 'STANDARD',
    promotion: emptyPromotion(),
    stock: '7',
    alertThreshold,
  };
}

function mountForm(props: Record<string, unknown> = {}) {
  return mount(ProductForm, {
    props: { initial: lamp(null), saving: false, skuAlreadyUsed: false, ...props },
    global: { plugins: [vuetify, i18n, router] },
  });
}

describe('ProductForm.vue', () => {
  beforeEach(() => setActivePinia(createPinia()));

  it('shows the alert threshold only when the initial values have one', () => {
    // Act
    const creation = mountForm();
    const edition = mountForm({ initial: lamp('5') });
    // Assert
    expect(creation.findComponent(AlertThresholdField).exists()).toBe(false);
    expect(edition.findComponent(AlertThresholdField).exists()).toBe(true);
  });

  it('submits the edited values', async () => {
    // Arrange
    const wrapper = mountForm({ initial: lamp('5') });
    const [price, , stock, threshold] = wrapper.findAll('input[type="number"]');
    await price.setValue('40');
    await stock.setValue('9');
    await threshold.setValue('2');
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    // Assert
    expect(wrapper.emitted('submit')![0][0]).toMatchObject({
      sku: 'LAMP-DESK',
      price: '40',
      stock: '9',
      alertThreshold: '2',
    });
  });

  it('submits no alert threshold when it is not asked', async () => {
    // Arrange
    const wrapper = mountForm();
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    // Assert
    expect(wrapper.emitted('submit')![0][0]).toMatchObject({ alertThreshold: null });
  });

  it('does not submit an invalid stock', async () => {
    // Arrange
    const wrapper = mountForm();
    await wrapper.findAll('input[type="number"]')[2].setValue('-1');
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    // Assert
    expect(wrapper.emitted('submit')).toBeUndefined();
  });

  it('reports that the SKU was typed again', async () => {
    // Act
    const wrapper = mountForm();
    await wrapper.find('.sku input').setValue('LAMP-2');
    // Assert
    expect(wrapper.emitted('skuChange')).toHaveLength(1);
  });

  it('shows the label of the submit button and the error message', () => {
    // Act
    const wrapper = mountForm({ submitLabel: 'Create product', errorMessage: 'Not saved' });
    // Assert
    expect(wrapper.find('[data-test="form-save"]').text()).toBe('Create product');
    expect(wrapper.text()).toContain('Not saved');
  });
});
