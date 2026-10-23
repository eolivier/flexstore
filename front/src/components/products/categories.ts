// Display information for the product categories returned by the API
export interface CategoryInfo {
  label: string;
  icon: string;
  tint: string;
}

const categories: Record<string, CategoryInfo> = {
  CLOTHING: { label: 'nav.clothing', icon: 'mdi-tshirt-crew-outline', tint: '#F6E3DA' },
  ELECTRONICS: { label: 'nav.electronics', icon: 'mdi-cellphone', tint: '#DFE6F3' },
  ACCESSORIES: { label: 'nav.accessories', icon: 'mdi-watch', tint: '#E3EDE3' },
};

const unknownCategory: CategoryInfo = { label: 'nav.allProducts', icon: 'mdi-package-variant', tint: '#EEECE7' };

export function categoryInfo(category: string | undefined): CategoryInfo {
  return (category && categories[category]) || unknownCategory;
}

// Categories shown on the home page, with their catalog page
export const shopCategories = [
  { code: 'CLOTHING', to: '/products/clothing' },
  { code: 'ELECTRONICS', to: '/products/electronics' },
  { code: 'ACCESSORIES', to: '/products/accessories' },
];
