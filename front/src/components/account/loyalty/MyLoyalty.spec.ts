import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import MyLoyalty from './MyLoyalty.vue';
import { useAuthStore } from '@/stores/auth.store';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

function signIn() {
  const authStore = useAuthStore();
  authStore.token = 'token';
  authStore.currentUser = { id: 'user-1', role: 'USER' } as typeof authStore.currentUser;
}

async function mountMyLoyalty() {
  const wrapper = mount(MyLoyalty, { global: { plugins: [vuetify, i18n] } });
  await flushPromises();
  return wrapper;
}

describe('MyLoyalty.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    signIn();
  });

  it('shows the balance of points of the customer', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve({ points: 89 }) });
    // Act
    const wrapper = await mountMyLoyalty();
    // Assert
    expect(wrapper.get('[data-testid="loyalty-balance"]').text()).toBe('89 points');
  });

  it('reports an error when the points could not be loaded', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 500 });
    // Act
    const wrapper = await mountMyLoyalty();
    // Assert
    expect(wrapper.text()).toContain('could not be loaded');
  });
});
