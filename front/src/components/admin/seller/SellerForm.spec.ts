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
    expect(values).toEqual([flexstore.name, flexstore.address, flexstore.siret, flexstore.vatNumber]);
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
    await inputs(wrapper)[2].setValue('123');
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
    expect(wrapper.text()).toContain('The shop refused the seller: check its SIRET and its VAT number.');
  });
});
