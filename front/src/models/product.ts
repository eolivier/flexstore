// The language of the catalog: the products are written in it, and shown in it when they are not translated
// into the language of the customer
export const DEFAULT_LANGUAGE = 'en';

// name and description: in the language asked to the API. sku: the Stock Keeping Unit, unique in the catalog
export interface Product {
  id?: string;
  sku: string;
  name?: string;
  description: string;
  category: string;
  price?: number;
  currency: string;
}

// The languages of the shop, the default one first
export const SHOP_LANGUAGES = [DEFAULT_LANGUAGE, 'fr'];

// The name and the description of a product in one language
export interface ProductTranslation {
  language: string;
  name: string;
  description: string;
}

// A product as the administrator edits it: its translations in every language, the default one included
export interface AdminProduct {
  id: string;
  sku: string;
  category: string;
  price: number;
  currency: string;
  translations: ProductTranslation[];
}

// A product as the administrator gives it, to create or edit it
export type ProductInput = Omit<AdminProduct, 'id'>;

// What the administrator enters for each language of the shop: a name and a description, maybe empty
export type ProductTexts = Record<string, { name: string; description: string }>;

// How the creation or the edition of a product ended: saved, refused because another product has its SKU, or
// failed
export type ProductSave = 'saved' | 'skuAlreadyUsed' | 'failed';
