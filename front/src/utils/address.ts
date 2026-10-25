// The parts of an address, as the API (null) or the forms (empty) give the missing ones
interface AddressParts {
  recipientType: string;
  firstName?: string | null;
  lastName?: string | null;
  companyName?: string | null;
  street: string;
  complement?: string | null;
  postalCode: string;
  city: string;
  country: string;
}

// An address on one line, to a person or to a company:
// "Jane Doe, 12 rue des Lilas, Bâtiment B, 75011 Paris, France" (without an empty complement)
export function formatAddress(address: AddressParts): string {
  const person = [address.firstName, address.lastName].filter(Boolean).join(' ');
  const recipient = address.recipientType === 'LEGAL_ENTITY' ? address.companyName : person;
  const city = [address.postalCode, address.city].filter(Boolean).join(' ');
  const parts = [recipient, address.street, address.complement, city, address.country];
  return parts.filter(Boolean).join(', ');
}
