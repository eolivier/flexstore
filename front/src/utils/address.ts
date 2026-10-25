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

// A person needs a first name and a last name, a company its name (the addresses created before the recipients
// have the names of their user, maybe without last name)
export function isRecipientComplete(address: AddressParts): boolean {
  if (address.recipientType === 'LEGAL_ENTITY') return !!address.companyName?.trim();
  return !!address.firstName?.trim() && !!address.lastName?.trim();
}

// Who receives what is sent to the address: the name of the company, or of the person
export function recipientName(address: AddressParts): string {
  const person = [address.firstName, address.lastName].filter(Boolean).join(' ');
  return address.recipientType === 'LEGAL_ENTITY' ? (address.companyName ?? '') : person;
}

// An address on one line, to a person or to a company:
// "Jane Doe, 12 rue des Lilas, Bâtiment B, 75011 Paris, France" (without an empty complement)
export function formatAddress(address: AddressParts): string {
  const recipient = recipientName(address);
  const city = [address.postalCode, address.city].filter(Boolean).join(' ');
  const parts = [recipient, address.street, address.complement, city, address.country];
  return parts.filter(Boolean).join(', ');
}
