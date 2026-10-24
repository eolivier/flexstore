import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import SignUp from './SignUp.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const session = { token: 'jwt-token', expiresAt: '2030-01-01T00:00:00Z', user: { id: 'u-1', firstName: 'Jane', lastName: 'Doe', email: 'jane@example.com', role: 'USER' } };

async function mountSignUp(redirect?: string) {
  const router = createRouter({ history: createMemoryHistory(), routes: [{ path: '/:any(.*)', component: { template: '<div />' } }] });
  await router.push({ path: '/sign-up', query: redirect ? { redirect } : {} });
  const wrapper = mount(SignUp, { global: { plugins: [vuetify, i18n, router] } });
  return { wrapper, router };
}

// Fills in the form: the text fields in their order on the screen, then the email, the password and the phone number
async function fillAndSubmit(wrapper: Awaited<ReturnType<typeof mountSignUp>>['wrapper']) {
  const [firstName, lastName, street, complement, postalCode, city] = wrapper.findAll('input[type="text"]');
  await firstName.setValue('Jane');
  await lastName.setValue('Doe');
  await wrapper.find('input[type="email"]').setValue('jane@example.com');
  await wrapper.find('input[type="password"]').setValue('password123');
  await wrapper.find('input[type="tel"]').setValue('+33 6 12 34 56 78');
  await street.setValue('12 rue des Lilas');
  await complement.setValue('Bâtiment B');
  await postalCode.setValue('75011');
  await city.setValue('Paris');
  await flushPromises();
  await wrapper.find('form').trigger('submit.prevent');
  await flushPromises();
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
      Promise.resolve(url.endsWith('/api/accounts')
        ? { ok: true, status: 201 }
        : { ok: true, status: 200, json: () => Promise.resolve(session) })
    );
  });

  it('sends the names, the email, the password, the phone number and the postal address', async () => {
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
      postalAddress: { street: '12 rue des Lilas', complement: 'Bâtiment B', postalCode: '75011', city: 'Paris', country: 'France' },
    });
  });

  it.each([
    { redirect: '/cart', expectedPath: '/cart' },
    { redirect: undefined, expectedPath: '/user-profile' },
  ])('once signed in, the customer goes to $expectedPath (redirect: $redirect)', async ({ redirect, expectedPath }) => {
    // Arrange
    const { wrapper, router } = await mountSignUp(redirect);
    // Act
    await fillAndSubmit(wrapper);
    // Assert
    expect(router.currentRoute.value.path).toBe(expectedPath);
  });

  it('reports an email already used', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 409 });
    const { wrapper } = await mountSignUp();
    // Act
    await fillAndSubmit(wrapper);
    // Assert
    expect(wrapper.text()).toContain('An account already exists with this email.');
  });

  it('links to the login, keeping where to go back', async () => {
    // Act
    const { wrapper } = await mountSignUp('/cart');
    // Assert
    const signIn = wrapper.findAll('a').find(link => link.text() === 'Sign in')!;
    expect(signIn.attributes('href')).toBe('/login?redirect=/cart');
  });
});
