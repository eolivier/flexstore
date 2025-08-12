import { useUsersStore } from '../stores/users'

export async function createUser() {
    // ...récupère les infos du formulaire
    const usersStore = useUsersStore()
    const user = {
      id: '1',
      name: 'Jean',
      email: 'jean@example.com'
    }
    await usersStore.addUser(user)
}
