import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import SellerSetupWizard from './SellerSetupWizard.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);
const mockPush = vi.fn();
vi.mock('vue-router', () => ({ useRouter: () => ({ push: mockPush }) }));

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
};
const enabledSettings = {
  orderConfirmationEmail: 'ENABLED',
  paymentDelayMinutes: 10,
  freeShippingThreshold: 50,
  sellerSetup: 'ENABLED',
};

async function mountWizard() {
  mockFetch.mockResolvedValueOnce({ ok: true, status: 200, json: async () => flexstore });
  const wrapper = mount(SellerSetupWizard, { global: { plugins: [vuetify, i18n] }, attachTo: document.body });
  await flushPromises();
  return wrapper;
}

const buttonLabelled = (wrapper: Awaited<ReturnType<typeof mountWizard>>, label: string) =>
  wrapper.findAll('button').find((button) => button.text() === label);

describe('SellerSetupWizard.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    mockPush.mockReset();
  });

  it('opens on the company step, filled with the current seller', async () => {
    // Act
    const wrapper = await mountWizard();
    // Assert
    const values = wrapper.findAll('input').map((input) => (input.element as HTMLInputElement).value);
    expect(values).toEqual([flexstore.name, flexstore.address, flexstore.legalForm, flexstore.shareCapital]);
  });

  it('cannot go to the next step while a field of the step is wrong', async () => {
    // Arrange
    const wrapper = await mountWizard();
    // Act
    await wrapper.findAll('input')[0].setValue('');
    // Assert
    expect(buttonLabelled(wrapper, 'Next')?.attributes('disabled')).toBeDefined();
  });

  it('finishing the setup saves the seller through the setup assistant, then opens the administration', async () => {
    // Arrange
    const wrapper = await mountWizard();
    await buttonLabelled(wrapper, 'Next')?.trigger('click');
    await buttonLabelled(wrapper, 'Next')?.trigger('click');
    await buttonLabelled(wrapper, 'Next')?.trigger('click');
    mockFetch.mockResolvedValueOnce({ ok: true, status: 200, json: async () => flexstore });
    mockFetch.mockResolvedValueOnce({ ok: true, status: 200, json: async () => enabledSettings });
    // Act
    await buttonLabelled(wrapper, 'Finish the setup')?.trigger('click');
    await flushPromises();
    // Assert
    expect(mockFetch.mock.calls[1][0]).toContain('/api/admin/seller/setup');
    expect(mockPush).toHaveBeenCalledWith('/admin');
  });

  it('stays on the assistant when the shop refuses the seller', async () => {
    // Arrange
    const wrapper = await mountWizard();
    await buttonLabelled(wrapper, 'Next')?.trigger('click');
    await buttonLabelled(wrapper, 'Next')?.trigger('click');
    await buttonLabelled(wrapper, 'Next')?.trigger('click');
    mockFetch.mockResolvedValueOnce({ ok: false, status: 400 });
    // Act
    await buttonLabelled(wrapper, 'Finish the setup')?.trigger('click');
    await flushPromises();
    // Assert
    expect(mockPush).not.toHaveBeenCalled();
    expect(wrapper.text()).toContain('The shop refused the seller');
  });
});
