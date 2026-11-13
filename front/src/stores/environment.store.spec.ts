import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useEnvironmentStore } from './environment.store';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const variables = [
  { name: 'FLEXSTORE_JWT_SECRET', sensitivity: 'SECRET', necessity: 'REQUIRED', status: 'DEFINED' },
  { name: 'FLEXSTORE_FRONT_URL', sensitivity: 'PLAIN', necessity: 'REQUIRED', status: 'DEFINED', value: 'https://shop' },
];

describe('environment.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('should read the environment variables', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(variables) });
    const store = useEnvironmentStore();
    // Act
    await store.fetchVariables();
    // Assert
    expect(mockFetch.mock.calls[0][0]).toBe('http://localhost:8080/api/admin/environment');
    expect(store.variables).toEqual(variables);
    expect(store.loaded).toBe(true);
  });

  it('should tell when the environment could not be loaded', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 500 });
    const store = useEnvironmentStore();
    // Act
    await store.fetchVariables();
    // Assert
    expect(store.loadError).toBe(true);
  });
});
