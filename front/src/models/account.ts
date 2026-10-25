// Who receives what is sent to an address, maybe someone else than the customer:
// a person (PRIVATE_INDIVIDUAL, by their names) or a company (LEGAL_ENTITY, by its name, VAT number optional)
export type RecipientType = 'PRIVATE_INDIVIDUAL' | 'LEGAL_ENTITY';

export interface Recipient {
  recipientType: RecipientType;
  firstName: string;
  lastName: string;
  companyName: string;
  vatNumber: string;
}

// Where an address is: the complement (building, floor...) is optional
export interface Location {
  street: string;
  complement: string;
  postalCode: string;
  city: string;
  country: string;
}

// A postal address: its recipient and where it is
export interface PostalAddress extends Recipient, Location {}

// The account a customer creates for themselves
export interface AccountRegistration {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phoneNumber: string;
  postalAddress: PostalAddress;
}
