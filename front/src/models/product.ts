// The language of the catalog: the products are written in it, and shown in it when they are not translated
// into the language of the customer
export const DEFAULT_LANGUAGE = 'en';

// name and description: in the language asked to the API
export interface Product {
  id?: string;
  name?: string;
  description: string;
  category: string;
  price?: number;
  currency: string;
}
