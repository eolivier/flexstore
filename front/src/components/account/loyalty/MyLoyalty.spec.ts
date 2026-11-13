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
    mockFetch.mockResolvedValue({
      ok: true,
      status: 200,
      json: () => Promise.resolve({ points: 89, purchases: [] }),
    });
    // Act
    const wrapper = await mountMyLoyalty();
    // Assert
    expect(wrapper.get('[data-testid="loyalty-balance"]').text()).toBe('89 points');
  });

  it('tells when the customer has no purchase yet', async () => {
    // Arrange
    mockFetch.mockResolvedValue({
      ok: true,
      status: 200,
      json: () => Promise.resolve({ points: 0, purchases: [] }),
    });
    // Act
    const wrapper = await mountMyLoyalty();
    // Assert
    expect(wrapper.find('[data-testid="loyalty-empty"]').exists()).toBe(true);
  });

  it('shows a line with the points of each purchase', async () => {
    // Arrange
    const purchases = [
      { orderNumber: 'AAAA1111', placedAt: '2026-10-24T10:00:00Z', points: 24 },
      { orderNumber: 'BBBB2222', placedAt: '2026-10-01T10:00:00Z', points: 1 },
    ];
    mockFetch.mockResolvedValue({
      ok: true,
      status: 200,
      json: () => Promise.resolve({ points: 25, purchases }),
    });
    // Act
    const wrapper = await mountMyLoyalty();
    // Assert
    const lines = wrapper.findAll('[data-testid="loyalty-purchase"]');
    expect(lines).toHaveLength(2);
    expect(lines[0].text()).toContain('AAAA1111');
    expect(lines[0].text()).toContain('+ 24 points');
    expect(lines[1].text()).toContain('+ 1 point');
    expect(wrapper.get('[data-testid="loyalty-balance"]').text()).toBe('25 points');
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
