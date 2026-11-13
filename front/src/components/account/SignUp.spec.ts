import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import SignUp from './SignUp.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const session = {
  token: 'jwt-token',
  expiresAt: '2030-01-01T00:00:00Z',
  user: { id: 'u-1', firstName: 'Jane', lastName: 'Doe', email: 'jane@example.com', role: 'USER' },
};

async function mountSignUp(redirect?: string) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
  });
  await router.push({ path: '/sign-up', query: redirect ? { redirect } : {} });
  const wrapper = mount(SignUp, { global: { plugins: [vuetify, i18n, router] } });
  return { wrapper, router };
}

type SignUpWrapper = Awaited<ReturnType<typeof mountSignUp>>['wrapper'];

// Fills in the express fields: the names, the email and the password
async function fillExpressFields(wrapper: SignUpWrapper) {
  const [firstName, lastName] = wrapper.findAll('input[type="text"]');
  await firstName.setValue('Jane');
  await lastName.setValue('Doe');
  await wrapper.find('input[type="email"]').setValue('jane@example.com');
  await wrapper.find('input[type="password"]').setValue('password123');
}

async function submit(wrapper: SignUpWrapper) {
  await flushPromises();
  await wrapper.find('form').trigger('submit.prevent');
  await flushPromises();
}

async function fillExpressAndSubmit(wrapper: SignUpWrapper) {
  await fillExpressFields(wrapper);
  await submit(wrapper);
}

// Fills in the form with the details: the text fields in their order on the screen (the fields of the company
// left out), then the email, the password and the phone number
async function fillAndSubmit(wrapper: SignUpWrapper) {
  await wrapper.get('[data-test="give-details"] input').setValue(true);
  const companyFields = '[data-test="company-name"], [data-test="siren"], [data-test="vat-number"]';
  const textFields = wrapper.findAll('input[type="text"]').filter((input) => !input.element.closest(companyFields));
  const [firstName, lastName, street, complement, postalCode, city] = textFields;
  await firstName.setValue('Jane');
  await lastName.setValue('Doe');
  await wrapper.find('input[type="email"]').setValue('jane@example.com');
  await wrapper.find('input[type="password"]').setValue('password123');
  await wrapper.find('input[type="tel"]').setValue('+33 6 12 34 56 78');
  await street.setValue('12 rue des Lilas');
  await complement.setValue('Bâtiment B');
  await postalCode.setValue('75011');
  await city.setValue('Paris');
  await submit(wrapper);
}

function sentRegistration() {
  const [, init] = mockFetch.mock.calls.find(([url]) => url.endsWith('/api/accounts'))!;
  return JSON.parse(init.body);
}

describe('SignUp.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    mockFetch.mockImplementation((url: string) =>
      Promise.resolve(
        url.endsWith('/api/accounts')
          ? { ok: true, status: 201 }
          : { ok: true, status: 200, json: () => Promise.resolve(session) }
      )
    );
  });

  it('should send only the names, the email and the password of an express registration', async () => {
    // Arrange
    const { wrapper } = await mountSignUp();
    // Act
    await fillExpressAndSubmit(wrapper);
    // Assert
    expect(sentRegistration()).toEqual({
      firstName: 'Jane',
      lastName: 'Doe',
      email: 'jane@example.com',
      password: 'password123',
    });
  });

  it('should not ask for the phone number and the address at first', async () => {
    // Act
    const { wrapper } = await mountSignUp();
    // Assert
    expect(wrapper.find('[data-test="sign-up-details"]').exists()).toBe(false);
  });

  it('should ask for the phone number and the address once the customer wants to give them', async () => {
    // Arrange
    const { wrapper } = await mountSignUp();
    // Act
    await wrapper.get('[data-test="give-details"] input').setValue(true);
    // Assert
    expect(wrapper.find('input[type="tel"]').exists()).toBe(true);
  });

  it('should sign the customer of an express registration in', async () => {
    // Arrange
    const { wrapper, router } = await mountSignUp();
    // Act
    await fillExpressAndSubmit(wrapper);
    // Assert
    expect(router.currentRoute.value.path).toBe('/user-profile');
  });

  it('should send the names, the email, the password, the phone number and the postal address', async () => {
    // Arrange
    const { wrapper } = await mountSignUp();
    // Act
    await fillAndSubmit(wrapper);
    // Assert
    expect(sentRegistration()).toEqual({
      firstName: 'Jane',
      lastName: 'Doe',
      email: 'jane@example.com',
      password: 'password123',
      phoneNumber: '+33 6 12 34 56 78',
      postalAddress: {
        recipientType: 'PRIVATE_INDIVIDUAL',
        firstName: 'Jane',
        lastName: 'Doe',
        companyName: '',
        vatNumber: '',
        street: '12 rue des Lilas',
        complement: 'Bâtiment B',
        postalCode: '75011',
        city: 'Paris',
        country: 'France',
      },
    });
  });

  it.each([
    { redirect: '/cart', expectedPath: '/cart' },
    { redirect: undefined, expectedPath: '/user-profile' },
  ])(
    'once signed in, the customer should go to $expectedPath (redirect: $redirect)',
    async ({ redirect, expectedPath }) => {
      // Arrange
      const { wrapper, router } = await mountSignUp(redirect);
      // Act
      await fillAndSubmit(wrapper);
      // Assert
      expect(router.currentRoute.value.path).toBe(expectedPath);
    }
  );

  it('should report an email already used', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 409 });
    const { wrapper } = await mountSignUp();
    // Act
    await fillAndSubmit(wrapper);
    // Assert
    expect(wrapper.text()).toContain('An account already exists with this email.');
  });

  it('should show the fields of the company once the customer says they are a company', async () => {
    // Arrange
    const { wrapper } = await mountSignUp();
    await wrapper.get('[data-test="give-details"] input').setValue(true);
    const fieldsBefore = wrapper.find('[data-test="siren"]').exists();
    // Act
    await wrapper.get('[data-test="is-company"] input').setValue(true);
    // Assert
    expect(fieldsBefore).toBe(false);
    expect(wrapper.find('[data-test="siren"]').exists()).toBe(true);
  });

  it('should send the company, its address sent to it for the attention of the customer', async () => {
    // Arrange
    const { wrapper } = await mountSignUp();
    await wrapper.get('[data-test="give-details"] input').setValue(true);
    await wrapper.get('[data-test="is-company"] input').setValue(true);
    await wrapper.get('[data-test="company-name"] input').setValue('Acme');
    await wrapper.get('[data-test="siren"] input').setValue('123 456 789');
    await wrapper.get('[data-test="vat-number"] input').setValue('fr32 123456789');
    // Act
    await fillAndSubmit(wrapper);
    // Assert
    const registration = sentRegistration();
    expect(registration.company).toEqual({ companyName: 'Acme', siren: '123456789', vatNumber: 'FR32123456789' });
    expect(registration.postalAddress.recipientType).toBe('LEGAL_ENTITY');
    expect(registration.postalAddress.companyName).toBe('Acme');
  });

  it('should report a company refused', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 400 });
    const { wrapper } = await mountSignUp();
    // Act
    await fillAndSubmit(wrapper);
    // Assert
    expect(wrapper.text()).toContain('The company was refused: check its SIREN and its VAT number.');
  });

  it('should link to the login, keeping where to go back', async () => {
    // Act
    const { wrapper } = await mountSignUp('/cart');
    // Assert
    const signIn = wrapper.findAll('a').find((link) => link.text() === 'Sign in')!;
    expect(signIn.attributes('href')).toBe('/login?redirect=/cart');
  });
});
