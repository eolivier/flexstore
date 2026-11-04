import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import TwoFactorAuthSection from './TwoFactorAuthSection.vue';
import { useAuthStore } from '@/stores/auth.store';
import { vuetify, i18n } from '@/test/setup';

// The rendering of the QR code itself is the library's concern: a fast, deterministic stub keeps this test
// focused on the enrollment flow
vi.mock('qrcode', () => ({ default: { toDataURL: vi.fn().mockResolvedValue('data:image/png;base64,stub') } }));

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

async function signedInAdmin(twoFactorEnabled = false) {
  mockFetch.mockResolvedValueOnce({
    ok: true,
    json: () =>
      Promise.resolve({
        status: 'SIGNED_IN',
        token: 'jwt-token',
        expiresAt: '2030-01-01T00:00:00Z',
        user: {
          id: 'admin-1',
          firstName: 'Jane',
          lastName: 'Doe',
          email: 'jane@example.com',
          role: 'ADMIN',
          twoFactorEnabled,
        },
      }),
  });
  await useAuthStore().login('jane@example.com', 'password123');
}

async function mountSection() {
  const wrapper = mount(TwoFactorAuthSection, { global: { plugins: [vuetify, i18n] }, attachTo: document.body });
  await flushPromises();
  return wrapper;
}

const buttonNamed = (name: string) =>
  [...document.querySelectorAll<HTMLButtonElement>('button')].find((button) => button.textContent?.trim() === name);

// The confirm button of a dialog, to tell it apart from a card button with the same label (e.g. "Disable")
const submitButtonNamed = (name: string) =>
  [...document.querySelectorAll<HTMLButtonElement>('button[type="submit"]')].find(
    (button) => button.textContent?.trim() === name
  );

describe('TwoFactorAuthSection.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    sessionStorage.clear();
    document.body.innerHTML = '';
  });

  it('offers to enable two-factor authentication when it is disabled', async () => {
    // Arrange
    await signedInAdmin(false);
    // Act
    const wrapper = await mountSection();
    // Assert
    expect(buttonNamed('Enable')).toBeDefined();
    expect(wrapper.text()).not.toContain('Two-factor authentication is enabled on your account.');
  });

  it('tells that two-factor authentication is enabled, with a way to disable it', async () => {
    // Arrange
    await signedInAdmin(true);
    // Act
    const wrapper = await mountSection();
    // Assert
    expect(wrapper.text()).toContain('Two-factor authentication is enabled on your account.');
    expect(buttonNamed('Disable')).toBeDefined();
  });

  it('enrolls, then enables two-factor authentication once the first code is confirmed', async () => {
    // Arrange
    await signedInAdmin(false);
    await mountSection();
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve({ secret: 'ABCDEFGHIJKLMNOP', provisioningUri: 'otpauth://totp/Flexstore:jane' }),
    });
    buttonNamed('Enable')!.click();
    await flushPromises();
    // Assert the secret and the QR code are shown
    expect(document.body.textContent).toContain('ABCDEFGHIJKLMNOP');
    expect(document.querySelector('img[alt="QR code"]')).not.toBeNull();
    // Act: confirm with the first code
    mockFetch.mockResolvedValueOnce({ ok: true, status: 204 });
    const codeField = [...document.querySelectorAll('input')].find((input) => input.maxLength === 6)!;
    codeField.value = '123456';
    codeField.dispatchEvent(new Event('input'));
    await flushPromises();
    submitButtonNamed('Confirm')!.click();
    await flushPromises();
    // Assert
    expect(useAuthStore().currentUser?.twoFactorEnabled).toBe(true);
    expect(document.body.textContent).toContain('Two-factor authentication is now enabled.');
  });

  it('disables two-factor authentication with the current password', async () => {
    // Arrange
    await signedInAdmin(true);
    await mountSection();
    buttonNamed('Disable')!.click();
    await flushPromises();
    mockFetch.mockResolvedValueOnce({ ok: true, status: 204 });
    const passwordField = document.querySelector('input[type="password"]') as HTMLInputElement;
    passwordField.value = 'password123';
    passwordField.dispatchEvent(new Event('input'));
    await flushPromises();
    // Act
    submitButtonNamed('Disable')!.click();
    await flushPromises();
    // Assert
    expect(useAuthStore().currentUser?.twoFactorEnabled).toBe(false);
    expect(document.body.textContent).toContain('Two-factor authentication is now disabled.');
  });
});
