import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import EditProduct from './EditProduct.vue';
import { vuetify, i18n } from '@/test/setup';
import { getProduct } from '@/services/product.service';

vi.mock('@/api', () => ({
  stocksApi: { getStocks: vi.fn(() => Promise.resolve({ data: [{ productId: 'p-1', quantity: 12 }] })) },
}));

vi.mock('@/services/product.service', () => ({
  getProduct: vi.fn(),
  getAllProducts: vi.fn(() => Promise.resolve([])),
}));

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});
const tShirt = {
  id: 'p-1',
  name: 'Organic T-shirt',
  description: 'Eco-friendly',
  category: 'CLOTHING',
  price: 25,
  currency: '€',
};

async function mountEditProduct() {
  const wrapper = mount(EditProduct, { props: { id: 'p-1' }, global: { plugins: [vuetify, i18n, router] } });
  await flushPromises();
  return wrapper;
}

describe('EditProduct.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.mocked(getProduct).mockResolvedValue(tShirt);
    mockFetch.mockReset();
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(tShirt) });
  });

  it('shows the current name, description and price of the product', async () => {
    // Act
    const wrapper = await mountEditProduct();
    // Assert
    expect((wrapper.find('input[type="text"]').element as HTMLInputElement).value).toBe('Organic T-shirt');
    expect((wrapper.find('textarea').element as HTMLTextAreaElement).value).toBe('Eco-friendly');
    const [price, stock] = wrapper.findAll('input[type="number"]');
    expect((price.element as HTMLInputElement).value).toBe('25');
    expect((stock.element as HTMLInputElement).value).toBe('12');
  });

  it('saves the edited product', async () => {
    // Arrange
    const wrapper = await mountEditProduct();
    await wrapper.find('input[type="text"]').setValue('Linen T-shirt');
    await wrapper.find('input[type="number"]').setValue('29.5');
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();
    // Assert
    const [url, init] = mockFetch.mock.calls.find(([, init]) => init?.method === 'PUT')!;
    expect(url).toMatch(/\/api\/products\/p-1$/);
    expect(JSON.parse(init.body)).toEqual({
      name: 'Linen T-shirt',
      description: 'Eco-friendly',
      category: 'CLOTHING',
      price: 29.5,
      currency: 'EUR',
    });
  });

  it('reports a product that could not be saved', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 404, text: () => Promise.resolve('') });
    const wrapper = await mountEditProduct();
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain('The product could not be saved.');
  });

  it('reports a product that cannot be loaded', async () => {
    // Arrange
    vi.mocked(getProduct).mockResolvedValue(null);
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
