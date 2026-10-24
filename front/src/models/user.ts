export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  password?: string;
  role?: 'USER' | 'ADMIN';
}

// "First name Last name", as the user is shown; a missing part is left out
export function fullName(user: Partial<Pick<User, 'firstName' | 'lastName'>> | null | undefined): string {
  return [user?.firstName, user?.lastName].filter(Boolean).join(' ');
}

// The initials of the first name and the last name (e.g. "JD" for Jane Doe)
export function initials(user: Partial<Pick<User, 'firstName' | 'lastName'>> | null | undefined): string {
  return [user?.firstName, user?.lastName].filter(Boolean).map(part => part![0].toUpperCase()).join('');
}
