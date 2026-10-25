import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import AdminProducts from './AdminProducts.vue';
import { vuetify, i18n } from '@/test/setup';
import { getAllProducts } from '@/services/product.service';

vi.mock('@/services/product.service', () => ({ getAllProducts: vi.fn(), getProduct: vi.fn() }));

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});
const products = [
  {
    id: 'p-1',
    name: 'Organic T-shirt',
    description: 'Eco-friendly',
    category: 'CLOTHING',
    price: 25,
    currency: '€',
  },
  {
    id: 'p-2',
    name: 'Smartwatch',
    description: 'Activity tracking',
    category: 'ACCESSORIES',
    price: 99,
    currency: '€',
  },
];

async function mountAdminProducts() {
  const wrapper = mount(AdminProducts, { global: { plugins: [vuetify, i18n, router] } });
  await flushPromises();
  return wrapper;
}

describe('AdminProducts.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.mocked(getAllProducts).mockResolvedValue(products);
  });

  it('lists the products of the catalog with their price', async () => {
    // Act
    const wrapper = await mountAdminProducts();
    // Assert
    expect(wrapper.text()).toContain('2 products');
    expect(wrapper.text()).toContain('Organic T-shirt');
    expect(wrapper.text()).toContain('99.00 €');
  });

  it.each([
    { name: 'Organic T-shirt', expectedLink: '/admin/products/p-1/edit' },
    { name: 'Smartwatch', expectedLink: '/admin/products/p-2/edit' },
  ])('links $name to its edition', async ({ name, expectedLink }) => {
    // Act
    const wrapper = await mountAdminProducts();
    // Assert
    expect(wrapper.find(`a[aria-label="Edit ${name}"]`).attributes('href')).toBe(expectedLink);
  });
});
