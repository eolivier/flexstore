// Who receives what is sent to an address, maybe someone else than the customer: the company is optional
export interface Recipient {
  firstName: string;
  lastName: string;
  companyName: string;
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
