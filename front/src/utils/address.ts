// The parts of an address, as the API (complement and company null) or the forms (empty) give them
interface AddressParts {
  firstName: string;
  lastName: string;
  companyName?: string | null;
  street: string;
  complement?: string | null;
  postalCode: string;
  city: string;
  country: string;
}

// An address on one line: "Jane Doe, Acme, 12 rue des Lilas, Bâtiment B, 75011 Paris, France"
// (without an empty company or complement)
export function formatAddress(address: AddressParts): string {
  const recipient = [address.firstName, address.lastName].filter(Boolean).join(' ');
  const city = [address.postalCode, address.city].filter(Boolean).join(' ');
  const parts = [recipient, address.companyName, address.street, address.complement, city, address.country];
  return parts.filter(Boolean).join(', ');
}
