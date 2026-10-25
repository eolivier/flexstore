import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import AddressBook from './AddressBook.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

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

// Answers the addresses, and the addresses after a change or a removal
function respondWith(addresses: object[], afterEdition: object[] = addresses, editionStatus = 200) {
  mockFetch.mockImplementation((_url: string, init?: RequestInit) => {
    const edited = init?.method === 'PUT' || init?.method === 'DELETE';
    const status = edited ? editionStatus : 200;
    const body = edited ? afterEdition : addresses;
    return Promise.resolve({ ok: status < 300, status, json: () => Promise.resolve(body) });
  });
}

async function mountAddressBook() {
  const wrapper = mount(AddressBook, {
    global: { plugins: [vuetify, i18n] },
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

  it('shows the addresses of the user', async () => {
    // Arrange
    respondWith([home, office]);
    // Act
    const wrapper = await mountAddressBook();
    // Assert
    expect(wrapper.text()).toContain('Jane Doe, 12 rue des Lilas, 75011 Paris, France');
    expect(wrapper.text()).toContain('Jane Doe, 3 place du Marché, 69001 Lyon, France');
  });

  it('tells a user without address where they come from', async () => {
    // Arrange
    respondWith([]);
    // Act
    const wrapper = await mountAddressBook();
    // Assert
    expect(wrapper.text()).toContain('No address yet');
  });

  it('removes an address', async () => {
    // Arrange
    respondWith([home, office], [office]);
    const wrapper = await mountAddressBook();
    // Act
    buttonNamed('Remove')[0].click();
    await flushPromises();
    // Assert
    const [url, init] = mockFetch.mock.calls[mockFetch.mock.calls.length - 1];
    expect(url).toMatch(/\/api\/accounts\/me\/addresses\/0$/);
    expect(init.method).toBe('DELETE');
    expect(wrapper.text()).not.toContain('12 rue des Lilas');
  });

  it('saves an edited address at its place', async () => {
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
