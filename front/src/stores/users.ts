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
    const apiUrl = import.meta.env.VITE_API_URL;
    const response = await fetch(`https://flexstore.onrender.com/api/users`);
    if (apiUrl) {
       console.info('apiUrl :', apiUrl);
    } else {
       console.error('VITE_API_URL non définie');
    }
    if (response.ok) {
      users.value = await response.json();
    } else {
      console.error('Erreur lors du chargement des utilisateurs');
    }
  }

  async function addUser(user: User) {
    const apiUrl = import.meta.env.VITE_API_URL;
    const response = await fetch(`${apiUrl}/api/users`, {
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
    const apiUrl = import.meta.env.VITE_API_URL;
    await fetch(`${apiUrl}/api/users/${id}`, { method: 'DELETE' });
    await fetchUsers();
  }

  return { users, fetchUsers, addUser, deleteUser };
});
