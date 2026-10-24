import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import EmailVerificationBanner from './EmailVerificationBanner.vue';
import { useAuthStore } from '@/stores/auth.store';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

async function signedInWithStatus(status: string) {
  mockFetch.mockResolvedValueOnce({
    ok: true,
    json: () => Promise.resolve({
      token: 'jwt-token',
      expiresAt: '2030-01-01T00:00:00Z',
      user: { id: 'u-1', firstName: 'Jane', lastName: 'Doe', email: 'jane@example.com', role: 'USER', status },
    }),
  });
  await useAuthStore().login('jane@example.com', 'password123');
  return mount(EmailVerificationBanner, { global: { plugins: [vuetify, i18n] } });
}

describe('EmailVerificationBanner.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    sessionStorage.clear();
  });

  it.each([
    { status: 'PENDING_VERIFICATION', shown: true },
    { status: 'ACTIVE', shown: false },
  ])('the banner is shown=$shown for an account $status', async ({ status, shown }) => {
    // Act
    const wrapper = await signedInWithStatus(status);
    // Assert
    expect(wrapper.text().includes('Verify your email address')).toBe(shown);
  });

  it('a pending customer can ask for a new link', async () => {
    // Arrange
    const wrapper = await signedInWithStatus('PENDING_VERIFICATION');
    mockFetch.mockResolvedValueOnce({ ok: true, status: 204 });
    // Act
    await wrapper.find('button').trigger('click');
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain('A new link has been sent. It is valid for 1 hour.');
  });
});
