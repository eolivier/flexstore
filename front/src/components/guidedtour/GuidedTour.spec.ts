import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import GuidedTour from './GuidedTour.vue';
import { useGuidedTourStore } from '@/stores/guidedTour.store';
import { guidedTourSections } from '@/utils/guidedTour';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

// The dialog is rendered in the body by Vuetify
const body = () => document.body.textContent ?? '';
const button = (label: string) =>
  [...document.body.querySelectorAll('button')].find((candidate) => candidate.textContent?.trim() === label);

async function openedTour() {
  const expiresAt = new Date(Date.now() + 3600_000).toISOString();
  const session = { token: 'token', expiresAt, user: { id: 'user-1', email: 'jane@flexstore.fr' } };
  sessionStorage.setItem('flexstore.session', JSON.stringify(session));
  mockFetch.mockResolvedValueOnce({ ok: true, status: 200, json: () => Promise.resolve({ guidedTour: 'NONE' }) });
  const store = useGuidedTourStore();
  const wrapper = mount(GuidedTour, { attachTo: document.body });
  await flushPromises();
  return { store, wrapper };
}

async function goToTheLastStep() {
  for (let step = 1; step < guidedTourSections.length; step++) {
    button('Next')?.click();
    await flushPromises();
  }
}

describe('GuidedTour.vue', () => {
  beforeEach(() => {
    document.body.innerHTML = '';
    sessionStorage.clear();
    mockFetch.mockReset();
    setActivePinia(createPinia());
  });

  it('should present the first section with its progress', async () => {
    // Act
    const { wrapper } = await openedTour();
    // Assert
    expect(body()).toContain('Browse the catalog');
    expect(body()).toContain(`Step 1 of ${guidedTourSections.length}`);
    wrapper.unmount();
  });

  it('should darken the page and bring the presented part into focus', async () => {
    // Arrange
    const { wrapper } = await openedTour();
    const cart = document.createElement('button');
    cart.dataset.guidedTour = 'cart';
    cart.getBoundingClientRect = () => ({ top: 10, left: 10, width: 40, height: 40 }) as DOMRect;
    document.body.appendChild(cart);
    // Assert: the first section has no visible part, the whole page is darkened
    expect(document.body.querySelector('[data-testid="guided-tour-hole"]')).toBeNull();
    // Act
    button('Next')?.click();
    await flushPromises();
    // Assert
    expect(document.body.querySelector('[data-testid="guided-tour-hole"]')).not.toBeNull();
    wrapper.unmount();
  });

  it('should go on to the next section and back', async () => {
    // Arrange
    const { wrapper } = await openedTour();
    // Act
    button('Next')?.click();
    await flushPromises();
    // Assert
    expect(body()).toContain('Fill your cart');
    button('Previous')?.click();
    await flushPromises();
    expect(body()).toContain('Browse the catalog');
    wrapper.unmount();
  });

  it('should offer "Do not show again" on the last step only', async () => {
    // Arrange
    const { wrapper } = await openedTour();
    expect(body()).not.toContain('Do not show again');
    // Act
    await goToTheLastStep();
    // Assert
    expect(body()).toContain('Do not show again');
    expect(button('Finish')).toBeDefined();
    wrapper.unmount();
  });

  it('should close when skipped, without recording anything', async () => {
    // Arrange
    const { store, wrapper } = await openedTour();
    // Act
    button('Skip')?.click();
    await flushPromises();
    // Assert
    expect(store.isOpen).toBe(false);
    expect(mockFetch).toHaveBeenCalledTimes(1);
    wrapper.unmount();
  });

  it('should hide the tour when finished with "Do not show again" checked', async () => {
    // Arrange
    const { store, wrapper } = await openedTour();
    await goToTheLastStep();
    mockFetch.mockResolvedValueOnce({ ok: true, status: 200, json: () => Promise.resolve({ guidedTour: 'HIDDEN' }) });
    // Act
    document.body.querySelector<HTMLInputElement>('input[type="checkbox"]')?.click();
    await flushPromises();
    button('Finish')?.click();
    await flushPromises();
    // Assert
    expect(store.isOpen).toBe(false);
    expect(store.isShown).toBe(false);
    wrapper.unmount();
  });
});
