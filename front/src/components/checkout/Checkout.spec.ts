import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import Checkout from './Checkout.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const home = {
  recipientType: 'PRIVATE_INDIVIDUAL' as const,
  firstName: 'Jane',
  lastName: 'Doe',
  street: '12 rue des Lilas',
  complement: 'Bâtiment B',
  postalCode: '75011',
  city: 'Paris',
  country: 'France',
};
const office = {
  recipientType: 'PRIVATE_INDIVIDUAL' as const,
  firstName: 'Jane',
  lastName: 'Doe',
  street: '3 place du Marché',
  complement: null,
  postalCode: '69001',
  city: 'Lyon',
  country: 'France',
};
const cart = {
  items: [
    {
      itemId: 'i-1',
      productId: '1',
      productName: 'Lamp',
      productPrice: 35,
      productQuantity: 2,
      itemPrice: 70,
    },
  ],
  totalItemsPrice: 70,
  itemsCurrency: '€',
};

// Answers the account (with the given addresses), the cart and the order
function respondWith(postalAddresses: object[], orderStatus = 201) {
  mockFetch.mockImplementation((url: string, init?: RequestInit) => {
    const body = url.endsWith('/api/accounts/me')
      ? { id: 'u-1', firstName: 'Jane', lastName: 'Doe', postalAddresses }
      : url.endsWith('/api/cart/cart-items')
        ? cart
        : { id: 'order-1' };
    const status = init?.method === 'POST' ? orderStatus : 200;
    return Promise.resolve({ ok: status < 300, status, json: () => Promise.resolve(body) });
  });
}

async function mountCheckout() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
  });
  await router.push('/checkout');
  const wrapper = mount(Checkout, { global: { plugins: [vuetify, i18n, router] } });
  await flushPromises();
  return { wrapper, router };
}

async function placeOrder(wrapper: Awaited<ReturnType<typeof mountCheckout>>['wrapper']) {
  await wrapper
    .findAll('button')
    .find((button) => button.text() === 'Place the order')!
    .trigger('click');
  await flushPromises();
}

function sentAddresses() {
  const [, init] = mockFetch.mock.calls.find(
    ([url, init]) => url.endsWith('/api/orders') && init?.method === 'POST'
  )!;
  return JSON.parse(init.body);
}

describe('Checkout.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('offers the addresses of the customer', async () => {
    // Arrange
    respondWith([home, office]);
    // Act
    const { wrapper } = await mountCheckout();
    // Assert
    expect(wrapper.text()).toContain('Jane Doe, 12 rue des Lilas, Bâtiment B, 75011 Paris, France');
    expect(wrapper.text()).toContain('Jane Doe, 3 place du Marché, 69001 Lyon, France');
  });

  it('by default, delivers and bills the order to the first address of the customer', async () => {
    // Arrange
    respondWith([home, office]);
    const { wrapper } = await mountCheckout();
    // Act
    await placeOrder(wrapper);
    // Assert
    expect(sentAddresses()).toEqual({
      deliveryAddress: { ...home, companyName: '', vatNumber: '' },
    });
  });

  it('bills the order to another address of the customer', async () => {
    // Arrange
    respondWith([home, office]);
    const { wrapper } = await mountCheckout();
    await wrapper.find('input[type="checkbox"]').setValue(false);
    await flushPromises();
    const billingRadios = wrapper.findAll('fieldset')[1].findAll('input[type="radio"]');
    await billingRadios[1].setValue(true);
    await flushPromises();
    // Act
    await placeOrder(wrapper);
    // Assert
    const billingAddress = { ...office, companyName: '', vatNumber: '', complement: '' };
    expect(sentAddresses()).toEqual({
      deliveryAddress: { ...home, companyName: '', vatNumber: '' },
      billingAddress,
    });
  });

  it('a company is billed without the names of a contact', async () => {
    // Arrange
    respondWith([home]);
    const { wrapper } = await mountCheckout();
    await wrapper.find('input[type="checkbox"]').setValue(false);
    await flushPromises();
    const billing = () => wrapper.findAll('fieldset')[1];
    const [, otherBillingAddress] = billing().findAll('input[type="radio"]');
    await otherBillingAddress.setValue(true);
    await flushPromises();
    const [iAmACompany] = billing().findAll('input[type="checkbox"]');
    await iAmACompany.setValue(true);
    const [companyName] = billing().findAll('input[type="text"]');
    await companyName.setValue('Acme');
    // Act
    await placeOrder(wrapper);
    // Assert
    const { billingAddress } = sentAddresses();
    expect(billingAddress).toMatchObject({
      recipientType: 'LEGAL_ENTITY',
      companyName: 'Acme',
      firstName: '',
      lastName: '',
    });
  });

  it('a customer without address enters the delivery address', async () => {
    // Arrange
    respondWith([]);
    // Act
    const { wrapper } = await mountCheckout();
    // Assert
    expect(wrapper.text()).toContain('Street address');
  });

  it('another address is sent by default to the customer, without company', async () => {
    // Arrange
    respondWith([]);
    // Act
    const { wrapper } = await mountCheckout();
    // Assert
    const [firstName, lastName, companyName] = wrapper.findAll('input[type="text"]');
    const recipient = [firstName, lastName, companyName].map(
      (input) => (input.element as HTMLInputElement).value
    );
    expect(recipient).toEqual(['Jane', 'Doe', '']);
  });

  it.each([
    { orderStatus: 201, expectedPath: '/orders/order-1/confirmation' },
    { orderStatus: 403, expectedPath: '/checkout' },
  ])(
    'an order answered $orderStatus leads to $expectedPath',
    async ({ orderStatus, expectedPath }) => {
      // Arrange
      respondWith([home], orderStatus);
      const { wrapper, router } = await mountCheckout();
      // Act
      await placeOrder(wrapper);
      // Assert
      expect(router.currentRoute.value.path).toBe(expectedPath);
    }
  );

  it('tells a refused customer why the order was not placed', async () => {
    // Arrange
    respondWith([home], 403);
    const { wrapper } = await mountCheckout();
    // Act
    await placeOrder(wrapper);
    // Assert
    expect(wrapper.text()).toContain('Your account does not allow ordering');
  });
});
