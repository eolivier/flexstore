import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useUsersStore } from './users.store';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const jane = { id: 'u-1', firstName: 'Jane', lastName: 'Doe', email: 'jane@example.com', role: 'USER' as const };

// Answers each API call by method and path
function respondWith(
  responses: Record<string, { status: number; body?: unknown; headers?: Record<string, string> }>
) {
  mockFetch.mockImplementation((url: string, init?: RequestInit) => {
    const key = Object.keys(responses).find((k) => {
      const [method, path] = k.split(' ');
      return (init?.method ?? 'GET') === method && url.endsWith(path);
    });
    const { status, body, headers } = key ? responses[key] : { status: 404, body: undefined, headers: {} };
    return Promise.resolve({
      ok: status < 300,
      status,
      headers: new Headers(headers),
      json: () => Promise.resolve(body),
    });
  });
}

describe('users.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('should read one user', async () => {
    // Arrange
    respondWith({ 'GET /api/users/u-1': { status: 200, body: jane } });
    const store = useUsersStore();
    // Act
    const user = await store.fetchUser('u-1');
    // Assert
    expect(user).toEqual(jane);
  });

  it.each([
    { status: 200, expected: 'saved' },
    { status: 409, expected: 'emailUsed' },
    { status: 403, expected: 'error' },
  ])('an edit answered $status is $expected', async ({ status, expected }) => {
    // Arrange
    respondWith({
      'PUT /api/users/u-1': { status, body: jane },
      'GET /api/users?page=0&size=20': { status: 200, body: [jane] },
    });
    const store = useUsersStore();
    // Act
    const edition = await store.updateUser('u-1', {
      firstName: 'Jane',
      lastName: 'Doe',
      email: 'jane@example.com',
      role: 'ADMIN',
    });
    // Assert
    expect(edition).toBe(expected);
  });

  it('should send the first name, the last name, the email and the role, but no password', async () => {
    // Arrange
    respondWith({
      'PUT /api/users/u-1': { status: 200, body: jane },
      'GET /api/users': { status: 200, body: [jane] },
    });
    const store = useUsersStore();
    // Act
    await store.updateUser('u-1', {
      firstName: 'Jane',
      lastName: 'Doe',
      email: 'jane@example.com',
      role: 'ADMIN',
    });
    // Assert
    const [, init] = mockFetch.mock.calls.find(([, init]) => init?.method === 'PUT')!;
    expect(JSON.parse(init.body)).toEqual({
      firstName: 'Jane',
      lastName: 'Doe',
      email: 'jane@example.com',
      role: 'ADMIN',
    });
  });

  it('should read the page asked of the users and the number of all the users', async () => {
    // Arrange
    respondWith({
      'GET /api/users?page=1&size=20': { status: 200, body: [jane], headers: { 'X-Total-Count': '21' } },
    });
    const store = useUsersStore();
    // Act
    await store.fetchUsers(1);
    // Assert
    expect(store.users).toEqual([jane]);
    expect(store.page).toBe(1);
    expect(store.total).toBe(21);
  });

  it('should show the page before when the last user of the last page was deleted', async () => {
    // Arrange
    respondWith({
      'GET /api/users?page=1&size=20': { status: 200, body: [], headers: { 'X-Total-Count': '20' } },
      'GET /api/users?page=0&size=20': { status: 200, body: [jane], headers: { 'X-Total-Count': '20' } },
    });
    const store = useUsersStore();
    // Act
    await store.fetchUsers(1);
    // Assert
    expect(store.page).toBe(0);
    expect(store.users).toEqual([jane]);
  });
});
