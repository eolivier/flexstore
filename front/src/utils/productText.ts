import type { Product, ProductText } from '@/models/product';

// The name and the description of the product in the language asked, or in the default language when the
// product is not translated into it
export function productTextIn(product: Product, language: string): ProductText {
  const translation = product.translations?.find((translated) => translated.language === language);
  return translation ?? { name: product.name ?? '', description: product.description };
}
