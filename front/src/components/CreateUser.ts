export async function createUser() {
    const user = {
        id: '1',
        name: 'Jean',
        email: 'jean@example.com'
    }
  const response = await fetch('/api/users', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(user)
  })
  if (response.ok) {
      const text = await response.text()
      if (text) {
          const data = JSON.parse(text)
          console.log('Utilisateur créé:', data)
      } else {
          console.log('Utilisateur créé, mais pas de contenu retourné')
      }
  } else {
    // Gérer l’erreur
    const error = await response.text()
    console.error('Erreur lors de la création', error)
  }
}
