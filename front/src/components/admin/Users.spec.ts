import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import Users from './Users.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});
const jane = { id: 'u-1', firstName: 'Jane', lastName: 'Doe', email: 'jane@example.com', role: 'USER' };
const systemUser = { id: 'system', firstName: 'System', lastName: 'System', email: 's@x.local', role: 'SYSTEM' };

async function mountUsers() {
  const wrapper = mount(Users, { global: { plugins: [vuetify, i18n, router] } });
  await flushPromises();
  return wrapper;
}

describe('Users.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    mockFetch.mockResolvedValue({
      ok: true,
      status: 200,
      headers: { get: () => '2' },
      json: () => Promise.resolve([jane, systemUser]),
    });
  });

  it('shows the translated label of the system role', async () => {
    // Act
    const wrapper = await mountUsers();
    // Assert
    expect(wrapper.text()).toContain(i18n.global.t('createUser.roles.SYSTEM.label'));
    expect(wrapper.text()).not.toContain('createUser.roles.SYSTEM.label');
  });

  it('offers to edit every user but the system one', async () => {
    // Act
    const wrapper = await mountUsers();
    // Assert
    expect(wrapper.find('a[href="/admin/users/u-1/edit"]').exists()).toBe(true);
    expect(wrapper.find('a[href="/admin/users/system/edit"]').exists()).toBe(false);
  });
});
