import { beforeEach, describe, expect, it } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useLoadingStore } from './loading.store';

// A request whose answer the test decides
function pendingRequest() {
  let answer!: () => void;
  const request = new Promise<void>(resolve => (answer = resolve));
  return { request, answer };
}

describe('loading.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('should be loading while a request is in progress', () => {
    // Arrange
    const store = useLoadingStore();
    const { request } = pendingRequest();
    // Act
    store.track(request);
    // Assert
    expect(store.isLoading).toBe(true);
  });

  it('should stop loading once every request has answered', async () => {
    // Arrange
    const store = useLoadingStore();
    const first = pendingRequest();
    const second = pendingRequest();
    const tracked = [store.track(first.request), store.track(second.request)];
    first.answer();
    await tracked[0];
    const loadingWithOneLeft = store.isLoading;
    // Act
    second.answer();
    await tracked[1];
    // Assert
    expect(loadingWithOneLeft).toBe(true);
    expect(store.isLoading).toBe(false);
  });

  it('should stop loading when a request fails', async () => {
    // Arrange
    const store = useLoadingStore();
    // Act
    await store.track(Promise.reject(new Error('Network error'))).catch(() => undefined);
    // Assert
    expect(store.isLoading).toBe(false);
  });
});
