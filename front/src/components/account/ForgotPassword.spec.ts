import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import ForgotPassword from './ForgotPassword.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

async function openPage(query: Record<string, string> = {}) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
  });
  await router.push({ path: '/forgot-password', query });
  const wrapper = mount(ForgotPassword, { global: { plugins: [vuetify, i18n, router] } });
  await flushPromises();
  return wrapper;
}

describe('ForgotPassword.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('the email typed on the login page is filled in', async () => {
    // Act
    const wrapper = await openPage({ email: 'jane@example.com' });
    // Assert
    expect((wrapper.find('input[type=email]').element as HTMLInputElement).value).toBe('jane@example.com');
  });

  it('a sent request says that a link was sent if the email has an account', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 204 });
    const wrapper = await openPage({ email: 'jane@example.com' });
    // Act
    await wrapper.find('form').trigger('submit');
    await flushPromises();
    // Assert
    expect(mockFetch.mock.calls[0][0]).toContain('/api/password-reset-requests');
    expect(JSON.parse(mockFetch.mock.calls[0][1].body)).toEqual({ email: 'jane@example.com' });
    expect(wrapper.text()).toContain('If an account exists for jane@example.com, we sent it a link.');
  });
});
