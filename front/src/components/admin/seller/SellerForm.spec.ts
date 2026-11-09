import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import SellerForm from './SellerForm.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const flexstore = {
  name: 'Flexstore SAS',
  address: '1 rue de la Démo, 75001 Paris, France',
  siret: '123 456 789 00012',
  vatNumber: 'FR12345678901',
  phone: '01 23 45 67 89',
  email: 'contact@flexstore.fr',
  website: 'www.flexstore.fr',
  legalForm: 'SAS',
  shareCapital: '10 000',
  registryCity: 'Paris',
  socialNetworks: {
    facebook: 'https://www.facebook.com/flexstore',
    instagram: '',
    x: '',
    tiktok: '',
    youtube: '',
    twitch: 'twitch.tv/flexstore',
  },
};

function mountForm() {
  return mount(SellerForm, { props: { seller: flexstore }, global: { plugins: [vuetify, i18n] } });
}

const inputs = (wrapper: ReturnType<typeof mountForm>) => wrapper.findAll('input');

describe('SellerForm.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('shows the current seller', () => {
    // Act
    const wrapper = mountForm();
    // Assert
    const values = inputs(wrapper).map((input) => (input.element as HTMLInputElement).value);
    expect(values).toEqual([
      flexstore.name,
      flexstore.address,
      flexstore.legalForm,
      flexstore.shareCapital,
      flexstore.siret,
      flexstore.registryCity,
      flexstore.vatNumber,
      flexstore.phone,
      flexstore.email,
      flexstore.website,
      flexstore.socialNetworks.facebook,
      '',
      '',
      '',
      '',
      flexstore.socialNetworks.twitch,
    ]);
  });

  const buttonLabelled = (wrapper: ReturnType<typeof mountForm>, label: string) =>
    wrapper.findAll('button').find((button) => button.text() === label)!;

  it('keeps Save enabled and Cancel disabled while nothing is modified', () => {
    // Act
    const wrapper = mountForm();
    // Assert
    expect(buttonLabelled(wrapper, 'Save').attributes('disabled')).toBeUndefined();
    expect(buttonLabelled(wrapper, 'Cancel').attributes('disabled')).toBeDefined();
  });

  it('enables Cancel once a value differs from the initial one', async () => {
    // Arrange
    const wrapper = mountForm();
    // Act
    await inputs(wrapper)[0].setValue('Flexstore Group');
    await flushPromises();
    // Assert
    expect(buttonLabelled(wrapper, 'Cancel').attributes('disabled')).toBeUndefined();
  });

  it('disables Cancel again when the initial value is typed back', async () => {
    // Arrange
    const wrapper = mountForm();
    await inputs(wrapper)[0].setValue('Flexstore Group');
    // Act
    await inputs(wrapper)[0].setValue(flexstore.name);
    await flushPromises();
    // Assert
    expect(buttonLabelled(wrapper, 'Cancel').attributes('disabled')).toBeDefined();
  });

  it('restores the initial values when cancelled', async () => {
    // Arrange
    const wrapper = mountForm();
    await inputs(wrapper)[0].setValue('Flexstore Group');
    await inputs(wrapper)[10].setValue('https://www.facebook.com/other');
    // Act
    await buttonLabelled(wrapper, 'Cancel').trigger('click');
    await flushPromises();
    // Assert
    expect((inputs(wrapper)[0].element as HTMLInputElement).value).toBe(flexstore.name);
    expect((inputs(wrapper)[10].element as HTMLInputElement).value).toBe(flexstore.socialNetworks.facebook);
    expect(buttonLabelled(wrapper, 'Cancel').attributes('disabled')).toBeDefined();
  });

  it('puts Cancel on the left of Save', () => {
    // Act
    const wrapper = mountForm();
    // Assert
    const labels = wrapper.findAll('button').map((button) => button.text());
    expect(labels.indexOf('Cancel')).toBe(labels.indexOf('Save') - 1);
  });

  it('saves the edited seller', async () => {
    // Arrange
    const renamed = { ...flexstore, name: 'Flexstore Group' };
    mockFetch.mockResolvedValueOnce({ ok: true, status: 200, json: () => Promise.resolve(renamed) });
    const wrapper = mountForm();
    await inputs(wrapper)[0].setValue('Flexstore Group');
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit');
    await flushPromises();
    // Assert
    expect(mockFetch.mock.calls[0][0]).toContain('/api/admin/seller');
    expect(JSON.parse(mockFetch.mock.calls[0][1].body)).toEqual(renamed);
    expect(wrapper.text()).toContain('Saved: the next invoices are issued with this seller.');
  });

  it('refuses a SIRET that is not 14 digits', async () => {
    // Arrange
    const wrapper = mountForm();
    // Act
    await inputs(wrapper)[4].setValue('123');
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain('A SIRET has 14 digits');
  });

  it('tells why the shop refused the seller', async () => {
    // Arrange
    mockFetch.mockResolvedValueOnce({ ok: false, status: 400, json: () => Promise.resolve({}) });
    const wrapper = mountForm();
    // Act
    await wrapper.find('form').trigger('submit');
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain(
      'The shop refused the seller: check its SIRET, its VAT number, its phone, its email, its website, its incorporation and its social networks.'
    );
  });

  it.each([
    { field: 10, example: 'https://www.facebook.com/flexstore' },
    { field: 11, example: 'https://www.instagram.com/flexstore' },
    { field: 12, example: 'https://x.com/flexstore' },
    { field: 13, example: 'https://www.tiktok.com/@flexstore' },
    { field: 14, example: 'https://www.youtube.com/@flexstore' },
    { field: 15, example: 'https://www.twitch.tv/flexstore' },
  ])('hints the address of the network of field $field with $example', async ({ field, example }) => {
    // Arrange
    const wrapper = mountForm();
    // Act
    await inputs(wrapper)[field].trigger('focus');
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain(`The address of the account, e.g. ${example}`);
  });

  it.each([
    { field: 3, value: 'dix mille', expected: 'An amount in euros, in digits' },
    { field: 5, value: ' ', expected: 'Required' },
    { field: 7, value: 'abc', expected: 'Invalid phone number' },
    { field: 8, value: 'contact.flexstore.fr', expected: 'Invalid email' },
    { field: 9, value: 'flexstore', expected: 'Invalid website' },
    { field: 10, value: 'facebook', expected: 'Invalid web address' },
  ])('refuses "$value" ($expected)', async ({ field, value, expected }) => {
    // Arrange
    const wrapper = mountForm();
    // Act
    await inputs(wrapper)[field].setValue(value);
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain(expected);
  });
});
