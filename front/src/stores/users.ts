import { defineStore } from 'pinia';
import { ref } from 'vue';

interface User {
  id: number;
  name: string;
  email: string;
}

export const useUsersStore = defineStore('users', () => {
  const users = ref<User[]>([]);

  async function fetchUsers() {
    const response = await fetch('/api/users');
    if (response.ok) {
      users.value = await response.json();
    } else {
      console.error('Erreur lors du chargement des utilisateurs');
    }
  }

  async function addUser(user: User) {
    const response = await fetch('/api/users', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(user),
    });
    if (response.ok) {
      const text = await response.text();
      if (text) {
        const data = JSON.parse(text);
        console.log('Utilisateur créé:', data);
      } else {
        console.log('Utilisateur créé, mais pas de contenu retourné');
      }
    } else {
      // Gérer l’erreur
      const error = await response.text();
      console.error('Erreur lors de la création', error);
    }
    await fetchUsers();
  }

  async function deleteUser(id: number) {
    await fetch(`/api/users/${id}`, { method: 'DELETE' });
    await fetchUsers();
  }

  return { users, fetchUsers, addUser, deleteUser };
});
