import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import VerifyEmail from './VerifyEmail.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

async function openLink(query: Record<string, string>) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
  });
  await router.push({ path: '/verify-email', query });
  const wrapper = mount(VerifyEmail, { global: { plugins: [vuetify, i18n, router] } });
  await flushPromises();
  return wrapper;
}

describe('VerifyEmail.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    sessionStorage.clear();
  });

  it.each([
    { status: 204, expected: 'Email address verified' },
    { status: 410, expected: 'Link expired' },
    { status: 404, expected: 'Invalid link' },
  ])('a link answered $status shows "$expected"', async ({ status, expected }) => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: status < 300, status });
    // Act
    const wrapper = await openLink({ token: 'the-token' });
    // Assert
    expect(wrapper.find('h1').text()).toBe(expected);
  });

  it('a link without token is invalid, without asking the server', async () => {
    // Act
    const wrapper = await openLink({});
    // Assert
    expect(wrapper.find('h1').text()).toBe('Invalid link');
    expect(mockFetch).not.toHaveBeenCalled();
  });

  it('an expired link offers a visitor to sign in to receive a new one', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 410 });
    // Act
    const wrapper = await openLink({ token: 'the-token' });
    // Assert
    const signIn = wrapper.findAll('a').find((link) => link.text() === 'Sign in to receive a new link')!;
    expect(signIn.attributes('href')).toBe('/login?redirect=/user-profile');
  });
});
