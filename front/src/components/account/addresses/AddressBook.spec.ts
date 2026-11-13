import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import AddressBook from './AddressBook.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

const home = {
  recipientType: 'PRIVATE_INDIVIDUAL',
  firstName: 'Jane',
  lastName: 'Doe',
  street: '12 rue des Lilas',
  postalCode: '75011',
  city: 'Paris',
  country: 'France',
};
const office = { ...home, street: '3 place du Marché', postalCode: '69001', city: 'Lyon' };
const workshop = { ...home, street: "5 rue de l'Atelier", postalCode: '13001', city: 'Marseille' };

// Answers the addresses, and the addresses after an addition, a change or a removal
function respondWith(addresses: object[], afterEdition: object[] = addresses, editionStatus = 200) {
  mockFetch.mockImplementation((_url: string, init?: RequestInit) => {
    const edited = init?.method === 'POST' || init?.method === 'PUT' || init?.method === 'DELETE';
    const status = edited ? editionStatus : 200;
    const body = edited ? afterEdition : addresses;
    return Promise.resolve({ ok: status < 300, status, json: () => Promise.resolve(body) });
  });
}

async function mountAddressBook() {
  const wrapper = mount(AddressBook, {
    global: { plugins: [vuetify, i18n, router] },
    attachTo: document.body,
  });
  await flushPromises();
  return wrapper;
}

const buttonNamed = (name: string) =>
  [...document.querySelectorAll('button')].filter((button) => button.textContent?.trim() === name);

describe('AddressBook.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    document.body.innerHTML = '';
  });

  it('should show the addresses of the user', async () => {
    // Arrange
    respondWith([home, office]);
    // Act
    const wrapper = await mountAddressBook();
    // Assert
    expect(wrapper.text()).toContain('Jane Doe, 12 rue des Lilas, 75011 Paris, France');
    expect(wrapper.text()).toContain('Jane Doe, 3 place du Marché, 69001 Lyon, France');
  });

  it('should tell a user without address where they come from', async () => {
    // Arrange
    respondWith([]);
    // Act
    const wrapper = await mountAddressBook();
    // Assert
    expect(wrapper.text()).toContain('No address yet');
  });

  it('should offer to add an address next to the title, and from the empty state', async () => {
    // Arrange
    respondWith([]);
    // Act
    await mountAddressBook();
    // Assert
    expect(buttonNamed('Add an address')).toHaveLength(2);
  });

  it('should offer to add an address next to the title when there are already some', async () => {
    // Arrange
    respondWith([home, office]);
    // Act
    await mountAddressBook();
    // Assert
    expect(buttonNamed('Add an address')).toHaveLength(1);
  });

  it('should add a new address, at the end of the addresses', async () => {
    // Arrange
    respondWith([home, office], [home, office, workshop], 201);
    const wrapper = await mountAddressBook();
    // Act
    buttonNamed('Add an address')[0].click();
    await flushPromises();
    buttonNamed('Save')[0].click();
    await flushPromises();
    // Assert
    const [url, init] = mockFetch.mock.calls[mockFetch.mock.calls.length - 1];
    expect(url).toMatch(/\/api\/accounts\/me\/addresses$/);
    expect(init.method).toBe('POST');
    expect(wrapper.text()).toContain('Marseille');
  });

  it('should ask for confirmation before removing an address', async () => {
    // Arrange
    respondWith([home, office]);
    await mountAddressBook();
    // Act
    buttonNamed('Remove')[0].click();
    await flushPromises();
    // Assert
    expect(document.body.textContent).toContain('Remove this address?');
    expect(mockFetch.mock.calls.some(([, init]) => init?.method === 'DELETE')).toBe(false);
  });

  it('should remove an address once confirmed', async () => {
    // Arrange
    respondWith([home, office], [office]);
    const wrapper = await mountAddressBook();
    buttonNamed('Remove')[0].click();
    await flushPromises();
    // Act
    buttonNamed('Confirm')[0].click();
    await flushPromises();
    // Assert
    const [url, init] = mockFetch.mock.calls[mockFetch.mock.calls.length - 1];
    expect(url).toMatch(/\/api\/accounts\/me\/addresses\/0$/);
    expect(init.method).toBe('DELETE');
    expect(wrapper.text()).not.toContain('12 rue des Lilas');
  });

  it('should save an edited address at its place', async () => {
    // Arrange
    const moved = { ...office, street: '8 avenue Foch' };
    respondWith([home, office], [home, moved]);
    const wrapper = await mountAddressBook();
    buttonNamed('Edit')[1].click();
    await flushPromises();
    // Act
    buttonNamed('Save')[0].click();
    await flushPromises();
    // Assert
    const [url, init] = mockFetch.mock.calls[mockFetch.mock.calls.length - 1];
    expect(url).toMatch(/\/api\/accounts\/me\/addresses\/1$/);
    expect(init.method).toBe('PUT');
    expect(JSON.parse(init.body)).toMatchObject({ street: '3 place du Marché', city: 'Lyon' });
    expect(wrapper.text()).toContain('8 avenue Foch');
  });
});
