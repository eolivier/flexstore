import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { AccountStatus, User } from '@/models/user';
import { useAuthStore } from './auth.store';

// The fields of a user an administrator edits: the password is changed by a separate operation
export type EditedUser = Pick<User, 'firstName' | 'lastName' | 'email'> & { role: NonNullable<User['role']> };
export type UserEdition = 'saved' | 'emailUsed' | 'error';

export const useUsersStore = defineStore('users', () => {
  const users = ref<User[]>([]);
  const loading = ref(false);
  // User management is reserved to admins: requests carry the token of the signed-in user
  const authStore = useAuthStore();

  async function fetchUsers() {
    loading.value = true;
    try {
      const response = await authStore.authFetch('/api/users');
      if (response.ok) {
        users.value = await response.json();
      } else {
        console.error('Error when loading users');
      }
    } finally {
      loading.value = false;
    }
  }

  // Returns whether the user was created
  async function addUser(user: Omit<User, 'id'>): Promise<boolean> {
    try {
      const response = await authStore.authFetch('/api/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(user),
      });
      if (!response.ok) {
        console.error('Error when creating user', await response.text());
        return false;
      }
      await fetchUsers();
      return true;
    } catch (error) {
      console.error('Error when creating user', error);
      return false;
    }
  }

  // Returns whether the user was deleted
  async function deleteUser(id: string): Promise<boolean> {
    try {
      const response = await authStore.authFetch(`/api/users/${id}`, { method: 'DELETE' });
      await fetchUsers();
      return response.ok;
    } catch (error) {
      console.error('Error when deleting user', error);
      return false;
    }
  }

  // One user, or null when it cannot be read
  async function fetchUser(id: string): Promise<User | null> {
    const response = await authStore.authFetch(`/api/users/${encodeURIComponent(id)}`).catch(() => null);
    return response?.ok ? response.json() : null;
  }

  async function updateUser(id: string, editedUser: EditedUser): Promise<UserEdition> {
    const response = await authStore
      .authFetch(`/api/users/${encodeURIComponent(id)}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(editedUser),
      })
      .catch(() => null);
    if (response?.status === 409) return 'emailUsed';
    if (!response?.ok) return 'error';
    await fetchUsers();
    return 'saved';
  }

  // An administrator activates, suspends, locks or disables the account of another user. Returns whether it changed
  async function changeStatus(id: string, status: AccountStatus): Promise<boolean> {
    const response = await authStore
      .authFetch(`/api/users/${encodeURIComponent(id)}/status`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status }),
      })
      .catch(() => null);
    if (!response?.ok) return false;
    await fetchUsers();
    return true;
  }

  return { users, loading, fetchUsers, fetchUser, addUser, updateUser, changeStatus, deleteUser };
});
