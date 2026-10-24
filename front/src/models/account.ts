// The postal address of a customer: the complement (building, floor...) is optional
export interface PostalAddress {
  street: string;
  complement: string;
  postalCode: string;
  city: string;
  country: string;
}

// The account a customer creates for themselves
export interface AccountRegistration {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phoneNumber: string;
  postalAddress: PostalAddress;
}
