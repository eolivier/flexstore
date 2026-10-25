import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import ResetPassword from './ResetPassword.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

async function openLink(query: Record<string, string>) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
  });
  await router.push({ path: '/reset-password', query });
  const wrapper = mount(ResetPassword, { global: { plugins: [vuetify, i18n, router] } });
  await flushPromises();
  return wrapper;
}

async function choosePassword(wrapper: Awaited<ReturnType<typeof openLink>>, password: string) {
  await wrapper.find('input[type=password]').setValue(password);
  await flushPromises();
  await wrapper.find('form').trigger('submit');
  await flushPromises();
}

describe('ResetPassword.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('a link without token is invalid, without asking the server', async () => {
    // Act
    const wrapper = await openLink({});
    // Assert
    expect(wrapper.find('h1').text()).toBe('Invalid link');
    expect(mockFetch).not.toHaveBeenCalled();
  });

  it('the new password is sent with the token of the link', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 204 });
    const wrapper = await openLink({ token: 'the-token' });
    // Act
    await choosePassword(wrapper, 'new-password');
    // Assert
    expect(JSON.parse(mockFetch.mock.calls[0][1].body)).toEqual({
      token: 'the-token',
      password: 'new-password',
    });
    expect(wrapper.find('h1').text()).toBe('Password changed');
  });

  it.each([
    { status: 410, expected: 'Link expired' },
    { status: 404, expected: 'Invalid link' },
  ])('a link answered $status shows "$expected" and offers a new link', async ({ status, expected }) => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status });
    const wrapper = await openLink({ token: 'the-token' });
    // Act
    await choosePassword(wrapper, 'new-password');
    // Assert
    expect(wrapper.find('h1').text()).toBe(expected);
    expect(wrapper.text()).toContain('Ask for a new link');
  });
});
