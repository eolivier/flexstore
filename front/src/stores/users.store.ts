import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { AccountStatus, User } from '@/models/user';
import { pageQuery, totalCountOf } from '@/utils/pagination';
import { useAuthStore } from './auth.store';
import { jsonRequest } from '@/utils/jsonRequest';

// The fields of a user an administrator edits: the password is changed by a separate operation
export type EditedUser = Pick<User, 'firstName' | 'lastName' | 'email'> & { role: NonNullable<User['role']> };
export type UserEdition = 'saved' | 'emailUsed' | 'error';

export const useUsersStore = defineStore('users', () => {
  // A page of the users: the page shown (from 0), and the number of all the users
  const users = ref<User[]>([]);
  const page = ref(0);
  const total = ref(0);
  const loading = ref(false);
  // User management is reserved to admins: requests carry the token of the signed-in user
  const authStore = useAuthStore();

  async function fetchUsers(pageNumber: number = page.value) {
    loading.value = true;
    try {
      const response = await authStore.authFetch(`/api/users?${pageQuery(pageNumber)}`);
      if (response.ok) {
        const pageUsers: User[] = await response.json();
        users.value = pageUsers;
        page.value = pageNumber;
        total.value = totalCountOf(response, pageUsers.length);
        // The last user of the last page was deleted: the page before is the last one now
        if (pageUsers.length === 0 && pageNumber > 0) await fetchUsers(pageNumber - 1);
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
      const response = await authStore.authFetch('/api/users', jsonRequest('POST', user));
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
      .authFetch(`/api/users/${encodeURIComponent(id)}`, jsonRequest('PUT', editedUser))
      .catch(() => null);
    if (response?.status === 409) return 'emailUsed';
    if (!response?.ok) return 'error';
    await fetchUsers();
    return 'saved';
  }

  // An administrator activates, suspends, locks or disables the account of another user.
  // Returns whether it changed
  async function changeStatus(id: string, status: AccountStatus): Promise<boolean> {
    const response = await authStore
      .authFetch(`/api/users/${encodeURIComponent(id)}/status`, jsonRequest('PUT', { status }))
      .catch(() => null);
    if (!response?.ok) return false;
    await fetchUsers();
    return true;
  }

  // An administrator disables the two-factor authentication of another user, without their current password.
  // Returns whether it was disabled
  async function disableTwoFactor(id: string): Promise<boolean> {
    const response = await authStore
      .authFetch(`/api/users/${encodeURIComponent(id)}/two-factor-authentication`, { method: 'PUT' })
      .catch(() => null);
    return !!response?.ok;
  }

  return {
    users,
    page,
    total,
    loading,
    fetchUsers,
    fetchUser,
    addUser,
    updateUser,
    changeStatus,
    disableTwoFactor,
    deleteUser,
  };
});
