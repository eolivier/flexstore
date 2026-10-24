export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  password?: string;
  role?: 'USER' | 'ADMIN';
}

type Named = Partial<Pick<User, 'firstName' | 'lastName'>> | null | undefined;

// The first name and the last name of a person: every concatenation of the two lives here
export class FullName {
  private constructor(readonly firstName: string, readonly lastName: string) {}

  static of(person: Named): FullName {
    return new FullName(person?.firstName ?? '', person?.lastName ?? '');
  }

  // "First name Last name"; a missing part is left out
  get value(): string {
    return this.parts().join(' ');
  }

  // The initials of the first name and the last name (e.g. "JD" for Jane Doe)
  get initials(): string {
    return this.parts().map(part => part[0].toUpperCase()).join('');
  }

  private parts(): string[] {
    return [this.firstName, this.lastName].filter(Boolean);
  }
}
