// The parts of an address, as the API (complement null) or the forms (complement '') give them
interface AddressParts {
  street?: string;
  complement?: string | null;
  postalCode?: string;
  city?: string;
  country?: string;
}

// An address on one line: "12 rue des Lilas, Bâtiment B, 75011 Paris, France" (without an empty complement)
export function formatAddress(address: AddressParts | null | undefined): string {
  if (!address) return '';
  const city = [address.postalCode, address.city].filter(Boolean).join(' ');
  return [address.street, address.complement, city, address.country].filter(Boolean).join(', ');
}
