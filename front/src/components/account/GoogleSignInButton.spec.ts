import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import GoogleSignInButton from './GoogleSignInButton.vue';
import { vuetify, i18n } from '@/test/setup';

// The google object that the script of Google Identity Services gives to the page
const initialize = vi.fn();
const renderButton = vi.fn();

const mountButton = async () => {
  const wrapper = mount(GoogleSignInButton, { global: { plugins: [vuetify, i18n] } });
  await flushPromises();
  return wrapper;
};

describe('GoogleSignInButton.vue', () => {
  beforeEach(() => {
    initialize.mockReset();
    renderButton.mockReset();
    window.google = { accounts: { id: { initialize, renderButton } } };
  });

  afterEach(() => {
    delete window.google;
    vi.unstubAllEnvs();
  });

  it('should show nothing without the client id of Google', async () => {
    // Arrange
    vi.stubEnv('VITE_GOOGLE_CLIENT_ID', '');
    // Act
    const wrapper = await mountButton();
    // Assert
    expect(wrapper.find('[data-testid="google-sign-in-button"]').exists()).toBe(false);
    expect(initialize).not.toHaveBeenCalled();
  });

  it('should render the button of Google with the client id of the shop', async () => {
    // Arrange
    vi.stubEnv('VITE_GOOGLE_CLIENT_ID', 'shop-client-id');
    // Act
    const wrapper = await mountButton();
    // Assert
    expect(wrapper.find('[data-testid="google-sign-in-button"]').exists()).toBe(true);
    expect(initialize).toHaveBeenCalledWith(expect.objectContaining({ client_id: 'shop-client-id' }));
    expect(renderButton).toHaveBeenCalledTimes(1);
  });

  it('should give the ID token of Google once the person chose their account', async () => {
    // Arrange
    vi.stubEnv('VITE_GOOGLE_CLIENT_ID', 'shop-client-id');
    const wrapper = await mountButton();
    // Act
    initialize.mock.calls[0][0].callback({ credential: 'google-id-token' });
    // Assert
    expect(wrapper.emitted('credential')).toEqual([['google-id-token']]);
  });
});
