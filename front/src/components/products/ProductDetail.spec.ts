import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import ProductDetail from './ProductDetail.vue';
import { vuetify, i18n } from '@/test/setup';
import { getAllProducts, getProduct } from '@/services/product.service';

vi.mock('@/services/product.service', () => ({ getAllProducts: vi.fn(), getProduct: vi.fn() }));
vi.mock('@/api', () => ({
  apiBaseUrl: 'https://api.example',
  productImagesApi: { getProductsWithImage: vi.fn(() => Promise.resolve({ data: [] })) },
  stocksApi: {
    getStocks: vi.fn(() =>
      Promise.resolve({
        data: [
          { productId: 'jeans', quantity: 3 },
          { productId: 'jacket', quantity: 0 },
          { productId: 'shirt', quantity: 20 },
        ],
      })
    ),
  },
}));

const product = (id: string, name: string) => ({
  id,
  name,
  description: name,
  category: 'CLOTHING',
  price: 25,
  currency: '€',
});
const tShirt = product('t-shirt', 'Organic T-shirt');
const catalog = [
  tShirt,
  product('jeans', 'Slim Jeans'),
  product('jacket', 'Waterproof Jacket'),
  product('shirt', 'Linen Shirt'),
];

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

async function mountProductDetail() {
  const wrapper = mount(ProductDetail, {
    props: { id: 't-shirt' },
    global: { plugins: [vuetify, i18n, router] },
  });
  await flushPromises();
  return wrapper;
}

describe('ProductDetail.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.mocked(getProduct).mockResolvedValue(tShirt);
    vi.mocked(getAllProducts).mockResolvedValue(catalog);
  });

  it('leaves the products out of stock out of the same category', async () => {
    // Act
    const wrapper = await mountProductDetail();
    // Assert
    expect(wrapper.text()).toContain('Slim Jeans');
    expect(wrapper.text()).toContain('Linen Shirt');
    expect(wrapper.text()).not.toContain('Waterproof Jacket');
  });

  it('shows the stock of the products of the same category', async () => {
    // Act
    const wrapper = await mountProductDetail();
    // Assert
    expect(wrapper.text()).toContain('Only 3 left in stock');
  });
});
