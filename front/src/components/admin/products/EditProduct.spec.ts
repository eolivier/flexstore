import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount, type VueWrapper } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import EditProduct from './EditProduct.vue';
import { vuetify, i18n } from '@/test/setup';

vi.mock('@/api', () => ({
  apiBaseUrl: 'https://api.example',
  productImagesApi: { getProductsWithImage: vi.fn(() => Promise.resolve({ data: [] })) },
  stocksApi: { getStocks: vi.fn(() => Promise.resolve({ data: [{ productId: 'p-1', quantity: 12 }] })) },
}));

vi.mock('@/services/product.service', () => ({ getAllProducts: vi.fn(() => Promise.resolve([])) }));

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});
const tShirt = {
  id: 'p-1',
  sku: 'CLO-1',
  category: 'CLOTHING',
  price: 25,
  currency: '€',
  translations: [
    { language: 'en', name: 'Organic T-shirt', description: 'Eco-friendly' },
    { language: 'fr', name: 'T-shirt bio', description: 'Écologique' },
  ],
};

// The name fields, one per language (the SKU is a text field too)
const NAME_INPUTS = '.v-text-field:not(.sku) input[type="text"]';
const SKU_INPUT = '.sku input';

// The fields of the language shown: those of the other languages are hidden
function shownFields(wrapper: VueWrapper, selector: string) {
  return wrapper.findAll(selector).filter((field) => !field.element.closest('[style*="display: none"]'));
}

// The body of the product saved by the page
function savedProduct() {
  const [, init] = mockFetch.mock.calls.find(([, init]) => init?.method === 'PUT')!;
  return JSON.parse(init.body);
}

async function mountEditProduct() {
  const wrapper = mount(EditProduct, { props: { id: 'p-1' }, global: { plugins: [vuetify, i18n, router] } });
  await flushPromises();
  return wrapper;
}

describe('EditProduct.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(tShirt) });
  });

  it('loads the product with its translations in every language', async () => {
    // Act
    await mountEditProduct();
    // Assert
    expect(mockFetch.mock.calls.some(([url]) => /\/api\/admin\/products\/p-1$/.test(url))).toBe(true);
  });

  it('shows the name and the description in the language of the chip chosen', async () => {
    // Arrange
    const wrapper = await mountEditProduct();
    const frenchChip = wrapper.findAll('.v-chip').find((chip) => chip.text().startsWith('FR'))!;
    // Act
    await frenchChip.trigger('click');
    // Assert
    const [shownName] = shownFields(wrapper, NAME_INPUTS);
    expect((shownName.element as HTMLInputElement).value).toBe('T-shirt bio');
  });

  it('removes a translation whose name and description are emptied', async () => {
    // Arrange
    const wrapper = await mountEditProduct();
    const [, frenchName] = wrapper.findAll(NAME_INPUTS);
    const [, frenchDescription] = wrapper.findAll('textarea:not(.v-textarea__sizer)');
    await frenchName.setValue('');
    await frenchDescription.setValue('');
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();
    // Assert
    expect(savedProduct().translations).toEqual([
      { language: 'en', name: 'Organic T-shirt', description: 'Eco-friendly' },
    ]);
  });

  it('shows the current name, description and price of the product', async () => {
    // Act
    const wrapper = await mountEditProduct();
    // Assert
    expect((wrapper.find(NAME_INPUTS).element as HTMLInputElement).value).toBe('Organic T-shirt');
    expect((wrapper.find('textarea').element as HTMLTextAreaElement).value).toBe('Eco-friendly');
    const [price, stock] = wrapper.findAll('input[type="number"]');
    expect((price.element as HTMLInputElement).value).toBe('25');
    expect((stock.element as HTMLInputElement).value).toBe('12');
  });

  it('saves the edited product', async () => {
    // Arrange
    const wrapper = await mountEditProduct();
    await wrapper.find(NAME_INPUTS).setValue('Linen T-shirt');
    await wrapper.find('input[type="number"]').setValue('29.5');
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();
    // Assert
    const [url] = mockFetch.mock.calls.find(([, init]) => init?.method === 'PUT')!;
    expect(url).toMatch(/\/api\/products\/p-1$/);
    expect(savedProduct()).toEqual({
      sku: 'CLO-1',
      category: 'CLOTHING',
      price: 29.5,
      currency: 'EUR',
      translations: [
        { language: 'en', name: 'Linen T-shirt', description: 'Eco-friendly' },
        { language: 'fr', name: 'T-shirt bio', description: 'Écologique' },
      ],
    });
  });

  it('reports a product that could not be saved', async () => {
    // Arrange
    mockFetch.mockImplementation((_url, init) =>
      Promise.resolve(
        init?.method === 'PUT'
          ? { ok: false, status: 404, text: () => Promise.resolve('') }
          : { ok: true, status: 200, json: () => Promise.resolve(tShirt) }
      )
    );
    const wrapper = await mountEditProduct();
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain('The product could not be saved.');
  });

  it('shows the SKU of the product', async () => {
    // Act
    const wrapper = await mountEditProduct();
    // Assert
    expect((wrapper.find(SKU_INPUT).element as HTMLInputElement).value).toBe('CLO-1');
  });

  it('saves the SKU typed, trimmed and in upper case', async () => {
    // Arrange
    const wrapper = await mountEditProduct();
    await wrapper.find(SKU_INPUT).setValue(' tshirt-organic-m ');
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();
    // Assert
    expect(savedProduct().sku).toBe('TSHIRT-ORGANIC-M');
  });

  it('says that the SKU is used by another product', async () => {
    // Arrange
    mockFetch.mockImplementation((_url, init) =>
      Promise.resolve(
        init?.method === 'PUT'
          ? { ok: false, status: 409, text: () => Promise.resolve('') }
          : { ok: true, status: 200, json: () => Promise.resolve(tShirt) }
      )
    );
    const wrapper = await mountEditProduct();
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();
    // Assert
    expect(wrapper.find('.sku').text()).toContain('This SKU is already used by another product');
    expect(wrapper.text()).not.toContain('The product could not be saved.');
  });

  it('reports a product that cannot be loaded', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 404, json: () => Promise.resolve({}) });
    // Act
    const wrapper = await mountEditProduct();
    // Assert
    expect(wrapper.text()).toContain('The product could not be loaded.');
  });

  it('the cancel button goes back to the list of products', async () => {
    // Act
    const wrapper = await mountEditProduct();
    // Assert
    const cancel = wrapper.findAll('a').find((link) => link.text() === 'Cancel')!;
    expect(cancel.attributes('href')).toBe('/admin/products');
  });

  it('saves the stock of the product with it', async () => {
    // Arrange
    const wrapper = await mountEditProduct();
    const [, stock] = wrapper.findAll('input[type="number"]');
    await stock.setValue('7');
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();
    // Assert
    const [url, init] = mockFetch.mock.calls.find(([url]) => url.includes('/api/admin/stocks/'))!;
    expect(url).toMatch(/\/api\/admin\/stocks\/p-1$/);
    expect(JSON.parse(init.body)).toEqual({ quantity: 7 });
  });
});
