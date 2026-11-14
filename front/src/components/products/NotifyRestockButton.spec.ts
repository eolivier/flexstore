import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import NotifyRestockButton from './NotifyRestockButton.vue';
import { useAuthStore } from '@/stores/auth.store';
import { useRestockSubscriptionsStore } from '@/stores/restockSubscriptions.store';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

function signIn() {
  const authStore = useAuthStore();
  authStore.token = 'token';
  authStore.currentUser = { id: 'user-1', role: 'USER' } as typeof authStore.currentUser;
}

function mountButton() {
  return mount(NotifyRestockButton, { props: { productId: 'p-1' }, global: { plugins: [vuetify, i18n] } });
}

describe('NotifyRestockButton.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    mockFetch.mockResolvedValue({ ok: true, status: 204, json: () => Promise.resolve([]) });
  });

  it('should be hidden for a visitor', () => {
    // Act
    const wrapper = mountButton();
    // Assert
    expect(wrapper.find('button').exists()).toBe(false);
  });

  it('should offer a signed-in customer to be notified', () => {
    // Arrange
    signIn();
    // Act
    const wrapper = mountButton();
    // Assert
    expect(wrapper.text()).toBe('Notify me when it is back');
  });

  it('should subscribe the customer when the button is clicked', async () => {
    // Arrange
    signIn();
    const wrapper = mountButton();
    // Act
    await wrapper.find('button').trigger('click');
    await flushPromises();
    // Assert
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringMatching(/\/api\/restock-subscriptions\/p-1$/),
      expect.objectContaining({ method: 'PUT' })
    );
    expect(wrapper.text()).toBe('You will be notified by email');
  });

  it('should say that the customer is already notified when they subscribed before', () => {
    // Arrange
    signIn();
    useRestockSubscriptionsStore().subscribedProductIds = ['p-1'];
    // Act
    const wrapper = mountButton();
    // Assert
    expect(wrapper.text()).toBe('You will be notified by email');
    expect(wrapper.find('button').attributes('disabled')).toBeDefined();
  });

  it('should unsubscribe the customer when they click on the bell', async () => {
    // Arrange
    signIn();
    useRestockSubscriptionsStore().subscribedProductIds = ['p-1'];
    const wrapper = mountButton();
    // Act
    await wrapper.find('button[aria-label="Stop notifying me"]').trigger('click');
    await flushPromises();
    // Assert
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringMatching(/\/api\/restock-subscriptions\/p-1$/),
      expect.objectContaining({ method: 'DELETE' })
    );
    expect(wrapper.text()).toBe('Notify me when it is back');
  });

  it('should show no bell before the customer subscribed', () => {
    // Arrange
    signIn();
    // Act
    const wrapper = mountButton();
    // Assert
    expect(wrapper.find('button[aria-label="Stop notifying me"]').exists()).toBe(false);
  });

  it('should tell the customer when the request failed', async () => {
    // Arrange
    signIn();
    mockFetch.mockResolvedValue({ ok: false, status: 500 });
    const wrapper = mountButton();
    // Act
    await wrapper.find('button').trigger('click');
    await flushPromises();
    // Assert
    expect(wrapper.find('[role="alert"]').text()).toBe('We could not register your request. Please try again.');
  });
});
