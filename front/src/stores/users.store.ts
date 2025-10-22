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

  async function addUser(user: User) {
    const response = await fetch(`${apiUrl}/api/users`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(user),
    });
    if (response.ok) {
      const text = await response.text();
      if (text) {
        const data = JSON.parse(text);
        console.log('User created:', data);
      } else {
        console.log('User created, but no content returned');
      }
    } else {
      // Gérer l’erreur
      const error = await response.text();
      console.error('Error when creating user', error);
    }
    await fetchUsers();
  }

  async function deleteUser(id: number) {
    await fetch(`${apiUrl}/api/users/${id}`, { method: 'DELETE' });
    await fetchUsers();
  }

  return { users, fetchUsers, addUser, deleteUser };
});
