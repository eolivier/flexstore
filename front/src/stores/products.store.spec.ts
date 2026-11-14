import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import i18n from '@/plugins/i18n';
import type { Product, ProductInput } from '@/models/product';
import { getAllProducts } from '@/services/product.service';
import { useProductsStore } from './products.store';

vi.mock('@/services/product.service', () => ({ getAllProducts: vi.fn() }));

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const deskLamp: Product = {
  id: 'p-1',
  sku: 'LAMP-1',
  name: 'Desk lamp',
  description: '',
  category: 'home',
  currency: 'EUR',
};
const deskLampInput: ProductInput = {
  sku: 'LAMP-1',
  category: 'home',
  price: 35,
  currency: 'EUR',
  translations: [{ language: 'en', name: 'Desk lamp', description: 'A lamp' }],
  vatRate: 'STANDARD',
};
const answer = (status: number, body?: unknown) =>
  mockFetch.mockResolvedValue({
    ok: status < 300,
    status,
    json: () => Promise.resolve(body),
    text: () => Promise.resolve('failure details'),
  });

// The store watches the language of the application: it is disposed after each test, so that it stops reacting
const createdStores: ReturnType<typeof useProductsStore>[] = [];
function productsStore() {
  const store = useProductsStore();
  createdStores.push(store);
  return store;
}

describe('products.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    vi.mocked(getAllProducts).mockReset();
    vi.mocked(getAllProducts).mockResolvedValue([deskLamp]);
    vi.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    createdStores.splice(0).forEach((store) => store.$dispose());
    i18n.global.locale.value = 'fr';
    vi.restoreAllMocks();
  });

  it('should load the products in the language of the customer', async () => {
    // Arrange
    const store = productsStore();
    // Act
    await store.fetchProducts();
    // Assert
    expect(getAllProducts).toHaveBeenCalledWith('fr');
    expect(store.products).toEqual([deskLamp]);
    expect(store.loading).toBe(false);
    expect(store.error).toBeNull();
  });

  it('should be loading while the products are being loaded', async () => {
    // Arrange
    let finishLoading: (products: Product[]) => void = () => {};
    vi.mocked(getAllProducts).mockReturnValue(new Promise((resolve) => (finishLoading = resolve)));
    const store = productsStore();
    // Act
    const loading = store.fetchProducts();
    const loadingWhileWaiting = store.loading;
    finishLoading([deskLamp]);
    await loading;
    // Assert
    expect(loadingWhileWaiting).toBe(true);
    expect(store.loading).toBe(false);
  });

  it('should report the message of the failure when the products cannot be loaded', async () => {
    // Arrange
    vi.mocked(getAllProducts).mockRejectedValue(new Error('Network Error'));
    const store = productsStore();
    // Act
    await store.fetchProducts();
    // Assert
    expect(store.error).toBe('Network Error');
    expect(store.loading).toBe(false);
  });

  it('should report an unknown error when the failure is not an error', async () => {
    // Arrange
    vi.mocked(getAllProducts).mockRejectedValue('boom');
    const store = productsStore();
    // Act
    await store.fetchProducts();
    // Assert
    expect(store.error).toBe('Unknown error while loading products');
  });

  it('should forget the previous error when the products are loaded again', async () => {
    // Arrange
    vi.mocked(getAllProducts).mockRejectedValueOnce(new Error('Network Error'));
    const store = productsStore();
    await store.fetchProducts();
    // Act
    await store.fetchProducts();
    // Assert
    expect(store.error).toBeNull();
    expect(store.products).toEqual([deskLamp]);
  });

  it('should load the products again when the language changes once they are loaded', async () => {
    // Arrange
    const store = productsStore();
    await store.fetchProducts();
    vi.mocked(getAllProducts).mockClear();
    // Act
    i18n.global.locale.value = 'en';
    await flushPromises();
    // Assert
    expect(getAllProducts).toHaveBeenCalledWith('en');
  });

  it('should not load the products when the language changes before they are loaded', async () => {
    // Arrange
    productsStore();
    // Act
    i18n.global.locale.value = 'en';
    await flushPromises();
    // Assert
    expect(getAllProducts).not.toHaveBeenCalled();
  });

  it('should propose the SKU given by the API', async () => {
    // Arrange
    answer(200, { sku: 'HOME-DESK-LAMP' });
    const store = productsStore();
    // Act
    const sku = await store.proposeSku('home', 'Desk lamp');
    // Assert
    expect(sku).toBe('HOME-DESK-LAMP');
    expect(mockFetch.mock.calls[0][0]).toContain('/api/admin/products/sku-proposal?category=home&name=Desk+lamp');
  });

  it('should propose no SKU when the API refuses', async () => {
    // Arrange
    answer(403);
    const store = productsStore();
    // Act
    const sku = await store.proposeSku('home', 'Desk lamp');
    // Assert
    expect(sku).toBeNull();
  });

  it('should propose no SKU when the API cannot be reached', async () => {
    // Arrange
    mockFetch.mockRejectedValue(new Error('offline'));
    const store = productsStore();
    // Act
    const sku = await store.proposeSku('home', 'Desk lamp');
    // Assert
    expect(sku).toBeNull();
  });

  it('should propose no SKU and log the failure when the answer cannot be read', async () => {
    // Arrange
    const unreadableAnswer = new SyntaxError('Unexpected end of JSON input');
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.reject(unreadableAnswer) });
    const store = productsStore();
    // Act
    const sku = await store.proposeSku('home', 'Desk lamp');
    // Assert
    expect(sku).toBeNull();
    expect(console.error).toHaveBeenCalledWith('Failed to propose a SKU', unreadableAnswer);
  });

  it('should read the product of an administrator', async () => {
    // Arrange
    const adminProduct = { id: 'p/1', ...deskLampInput };
    answer(200, adminProduct);
    const store = productsStore();
    // Act
    const product = await store.fetchAdminProduct('p/1');
    // Assert
    expect(product).toEqual(adminProduct);
    expect(mockFetch.mock.calls[0][0]).toContain('/api/admin/products/p%2F1');
  });

  it('should read no product of an administrator when the API refuses', async () => {
    // Arrange
    answer(404);
    const store = productsStore();
    // Act
    const product = await store.fetchAdminProduct('p-1');
    // Assert
    expect(product).toBeNull();
  });

  it('should read no product of an administrator when the API cannot be reached', async () => {
    // Arrange
    mockFetch.mockRejectedValue(new Error('offline'));
    const store = productsStore();
    // Act
    const product = await store.fetchAdminProduct('p-1');
    // Assert
    expect(product).toBeNull();
  });

  it('should keep the id given by the API when a product is created', async () => {
    // Arrange
    answer(201, deskLamp);
    const store = productsStore();
    // Act
    const creation = await store.addProduct(deskLampInput);
    // Assert
    expect(creation).toEqual({ outcome: 'saved', productId: 'p-1' });
    expect(mockFetch.mock.calls[0][0]).toContain('/api/products');
    expect(mockFetch.mock.calls[0][1].method).toBe('POST');
    expect(JSON.parse(mockFetch.mock.calls[0][1].body)).toEqual(deskLampInput);
  });

  it('should fail to create a product when the API answers without its id', async () => {
    // Arrange
    answer(201, { ...deskLamp, id: undefined });
    const store = productsStore();
    // Act
    const creation = await store.addProduct(deskLampInput);
    // Assert
    expect(creation).toEqual({ outcome: 'failed' });
    expect(getAllProducts).not.toHaveBeenCalled();
    expect(console.error).toHaveBeenCalledWith('Failed to save the product', 'the answer has no product id');
  });

  it('should load the products again once a product is created', async () => {
    // Arrange
    answer(201, deskLamp);
    const store = productsStore();
    // Act
    await store.addProduct(deskLampInput);
    // Assert
    expect(getAllProducts).toHaveBeenCalledTimes(1);
    expect(store.products).toEqual([deskLamp]);
  });

  it('should refuse to create a product whose SKU is used by another one', async () => {
    // Arrange
    answer(409);
    const store = productsStore();
    // Act
    const creation = await store.addProduct(deskLampInput);
    // Assert
    expect(creation).toEqual({ outcome: 'skuAlreadyUsed' });
    expect(getAllProducts).not.toHaveBeenCalled();
  });

  it('should fail to create a product when the API fails', async () => {
    // Arrange
    answer(500);
    const store = productsStore();
    // Act
    const creation = await store.addProduct(deskLampInput);
    // Assert
    expect(creation).toEqual({ outcome: 'failed' });
    expect(getAllProducts).not.toHaveBeenCalled();
  });

  it('should fail to create a product when the API cannot be reached', async () => {
    // Arrange
    mockFetch.mockRejectedValue(new Error('offline'));
    const store = productsStore();
    // Act
    const creation = await store.addProduct(deskLampInput);
    // Assert
    expect(creation).toEqual({ outcome: 'failed' });
  });

  it('should update a product at its address', async () => {
    // Arrange
    answer(200, deskLamp);
    const store = productsStore();
    // Act
    const update = await store.updateProduct('p-1', deskLampInput);
    // Assert
    expect(update).toBe('saved');
    expect(mockFetch.mock.calls[0][0]).toContain('/api/products/p-1');
    expect(mockFetch.mock.calls[0][1].method).toBe('PUT');
  });

  it('should fail to update a product when the API answers without its id', async () => {
    // Arrange
    answer(200, { ...deskLamp, id: undefined });
    const store = productsStore();
    // Act
    const update = await store.updateProduct('p-1', deskLampInput);
    // Assert
    expect(update).toBe('failed');
  });

  it('should refuse to update a product to a SKU used by another one', async () => {
    // Arrange
    answer(409);
    const store = productsStore();
    // Act
    const update = await store.updateProduct('p-1', deskLampInput);
    // Assert
    expect(update).toBe('skuAlreadyUsed');
  });

  it('should fail to update a product when the API fails', async () => {
    // Arrange
    answer(500);
    const store = productsStore();
    // Act
    const update = await store.updateProduct('p-1', deskLampInput);
    // Assert
    expect(update).toBe('failed');
  });
});
