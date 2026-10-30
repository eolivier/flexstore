import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount, type VueWrapper } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import CreateProduct from './CreateProduct.vue';
import { vuetify, i18n } from '@/test/setup';

vi.mock('@/services/product.service', () => ({ getAllProducts: vi.fn(() => Promise.resolve([])) }));

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

beforeAll(() => {
  URL.createObjectURL = () => 'blob:preview';
  URL.revokeObjectURL = () => undefined;
});

// The API answers the created product, and saves its image and its stock (unless told otherwise)
function respond(imageSaved = true) {
  mockFetch.mockImplementation((url: string, init?: RequestInit) => {
    if (url.endsWith('/api/products') && init?.method === 'POST') {
      return Promise.resolve({ ok: true, status: 201, json: () => Promise.resolve({ id: 'p-9' }) });
    }
    if (url.includes('/image')) return Promise.resolve({ ok: imageSaved, status: imageSaved ? 204 : 500 });
    if (url.includes('/api/admin/stocks/')) {
      return Promise.resolve({ ok: true, json: () => Promise.resolve({ productId: 'p-9', quantity: 7 }) });
    }
    return Promise.resolve({ ok: true, status: 200, json: () => Promise.resolve([]) });
  });
}

async function fillProduct(wrapper: VueWrapper) {
  await wrapper.find('.sku input').setValue('LAMP-DESK');
  await wrapper.find('.v-text-field:not(.sku) input[type="text"]').setValue('Desk lamp');
  await wrapper.find('textarea:not(.v-textarea__sizer)').setValue('Warm light');
  const [price, stock] = wrapper.findAll('input[type="number"]');
  await price.setValue('35');
  await stock.setValue('7');
  const imageInput = wrapper.find('[data-test="image-input"]');
  const photo = new File(['photo'], 'lamp.jpg', { type: 'image/jpeg' });
  Object.defineProperty(imageInput.element, 'files', { value: [photo], configurable: true });
  await imageInput.trigger('change');
  await flushPromises();
}

function callTo(path: string) {
  return mockFetch.mock.calls.find(([url]) => url.includes(path));
}

describe('CreateProduct.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('the cancel button goes back to the list of products', () => {
    // Act
    const wrapper = mount(CreateProduct, { global: { plugins: [vuetify, i18n, router] } });
    // Assert
    const cancel = wrapper.findAll('a').find((link) => link.text() === 'Cancel')!;
    expect(cancel.attributes('href')).toBe('/admin/products');
  });

  it('creates the product, then gives it its image and its stock', async () => {
    // Arrange
    respond();
    const wrapper = mount(CreateProduct, { global: { plugins: [vuetify, i18n, router] } });
    await fillProduct(wrapper);
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();
    // Assert
    const [, imageInit] = callTo('/api/admin/products/p-9/image')!;
    expect(imageInit.method).toBe('PUT');
    const [, stockInit] = callTo('/api/admin/stocks/p-9')!;
    expect(JSON.parse(stockInit.body)).toEqual({ quantity: 7 });
    expect(document.body.textContent).toContain('The product Desk lamp has been created.');
  });

  it('says that the product is created, but that its image could not be saved', async () => {
    // Arrange
    respond(false);
    const wrapper = mount(CreateProduct, { global: { plugins: [vuetify, i18n, router] } });
    await fillProduct(wrapper);
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();
    // Assert
    expect(document.body.textContent).toContain('its image or its stock could not be saved');
  });
});
