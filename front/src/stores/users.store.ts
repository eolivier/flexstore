import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { User } from '@/models/user';

export const useUsersStore = defineStore('users', () => {
  const users = ref<User[]>([]);
  const apiUrl = import.meta.env.VITE_API_URL;

  async function fetchUsers() {
      const response = await fetch(`${apiUrl}/api/users`);
    if (response.ok) {
      users.value = await response.json();
    } else {
      console.error('Error when loading users');
    }
  }

  // Returns whether the user was created
  async function addUser(user: Omit<User, 'id'>): Promise<boolean> {
    try {
      const response = await fetch(`${apiUrl}/api/users`, {
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
      const response = await fetch(`${apiUrl}/api/users/${id}`, { method: 'DELETE' });
      await fetchUsers();
      return response.ok;
    } catch (error) {
      console.error('Error when deleting user', error);
      return false;
    }
  }

  return { users, fetchUsers, addUser, deleteUser };
});
