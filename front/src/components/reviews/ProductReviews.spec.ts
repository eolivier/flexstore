import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import ProductReviews from './ProductReviews.vue';
import { useAuthStore } from '@/stores/auth.store';
import { vuetify, i18n } from '@/test/setup';

vi.mock('@/api', () => ({
  apiBaseUrl: 'https://api.example',
  reviewsApi: {
    getRatings: vi.fn(() => Promise.resolve({ data: [] })),
    getReviews: vi.fn(() => Promise.resolve({ data: [] })),
  },
}));

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

function signIn() {
  const authStore = useAuthStore();
  authStore.token = 'token';
  authStore.currentUser = { id: 'user-1', role: 'USER' } as typeof authStore.currentUser;
}

async function mountReviews() {
  const wrapper = mount(ProductReviews, {
    props: { productId: 'lamp' },
    global: { plugins: [vuetify, i18n, router] },
  });
  await flushPromises();
  return wrapper;
}

describe('ProductReviews.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('offers the form to a customer who bought the product', async () => {
    // Arrange
    signIn();
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve({ eligibility: 'CAN_REVIEW' }) });
    // Act
    const wrapper = await mountReviews();
    // Assert
    expect(wrapper.find('form').exists()).toBe(true);
  });

  it.each([
    { eligibility: 'NOT_BOUGHT', shown: 'not-bought' },
    { eligibility: 'ALREADY_REVIEWED', shown: 'already-reviewed' },
  ])('tells a customer why there is no form ($eligibility)', async ({ eligibility, shown }) => {
    // Arrange
    signIn();
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve({ eligibility }) });
    // Act
    const wrapper = await mountReviews();
    // Assert
    expect(wrapper.find('form').exists()).toBe(false);
    expect(wrapper.find(`[data-test="${shown}"]`).exists()).toBe(true);
  });

  it('asks a visitor to sign in, without asking the back end', async () => {
    // Act
    const wrapper = await mountReviews();
    // Assert
    expect(wrapper.find('form').exists()).toBe(false);
    expect(wrapper.text()).toContain('Sign in to review a product you bought.');
    expect(mockFetch).not.toHaveBeenCalled();
  });
});
