// Display information for the product categories returned by the API
export interface CategoryInfo {
  label: string;
  icon: string;
  tint: string;
}

// A color of the current theme (light or dark), as a CSS value
function themeColor(name: string): string {
  return `rgb(var(--v-theme-${name}))`;
}

const categories: Record<string, CategoryInfo> = {
  CLOTHING: { label: 'nav.clothing', icon: 'mdi-tshirt-crew-outline', tint: themeColor('tint-clothing') },
  ELECTRONICS: { label: 'nav.electronics', icon: 'mdi-cellphone', tint: themeColor('tint-electronics') },
  ACCESSORIES: { label: 'nav.accessories', icon: 'mdi-watch', tint: themeColor('tint-accessories') },
};

const unknownCategory: CategoryInfo = {
  label: 'nav.allProducts',
  icon: 'mdi-package-variant',
  tint: themeColor('tint-other'),
};

// A category that the shop knows, with a label of its own
export function isKnownCategory(category: string): boolean {
  return category in categories;
}

export function categoryInfo(category: string | undefined): CategoryInfo {
  return (category && categories[category]) || unknownCategory;
}

// Categories shown on the home page, with their catalog page
export const shopCategories = [
  { code: 'CLOTHING', to: '/products/clothing' },
  { code: 'ELECTRONICS', to: '/products/electronics' },
  { code: 'ACCESSORIES', to: '/products/accessories' },
];
