// The name and the description of a product in one language (en, fr)
export interface ProductTranslation {
  language: string;
  name: string;
  description: string;
}

// name and description: in the default language; translations: in every language of the product
export interface Product {
  id?: string;
  name?: string;
  description: string;
  category: string;
  price?: number;
  currency: string;
  translations?: ProductTranslation[];
}

// What a product says of itself, in the language shown
export interface ProductText {
  name: string;
  description: string;
}
