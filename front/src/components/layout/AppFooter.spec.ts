import { beforeEach, describe, expect, it, vi } from 'vitest';
import { mount, RouterLinkStub } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import AppFooter from './AppFooter.vue';
import { useAuthStore } from '@/stores/auth.store';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

function mountFooter() {
  return mount(AppFooter, { global: { plugins: [vuetify, i18n], stubs: { RouterLink: RouterLinkStub } } });
}

function linkTargets(wrapper: ReturnType<typeof mountFooter>) {
  return wrapper.findAllComponents(RouterLinkStub).map((link) => link.props('to'));
}

describe('AppFooter.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    sessionStorage.clear();
  });

  it('a visitor who is not signed in has no link to the orders or the account', () => {
    // Act
    const wrapper = mountFooter();
    // Assert
    expect(linkTargets(wrapper)).not.toContain('/orders');
    expect(linkTargets(wrapper)).not.toContain('/user-profile');
  });

  it('a signed-in customer has the links to their orders and their account', async () => {
    // Arrange
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: () =>
        Promise.resolve({
          token: 'jwt-token',
          expiresAt: '2030-01-01T00:00:00Z',
          user: { id: 'u-1', firstName: 'Jane', lastName: 'Doe', email: 'jane@example.com', role: 'USER' },
        }),
    });
    await useAuthStore().login('jane@example.com', 'password123');
    // Act
    const wrapper = mountFooter();
    // Assert
    expect(linkTargets(wrapper)).toEqual(expect.arrayContaining(['/orders', '/user-profile']));
  });
});
