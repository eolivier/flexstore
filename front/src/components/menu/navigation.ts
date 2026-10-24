// Shared navigation entries for the header, the mobile drawer and the footer
export const shopLinks = [
  { to: '/products/', icon: 'mdi-view-grid-outline', label: 'nav.allProducts' },
  { to: '/products/clothing', icon: 'mdi-tshirt-crew-outline', label: 'nav.clothing' },
  { to: '/products/electronics', icon: 'mdi-cellphone', label: 'nav.electronics' },
  { to: '/products/accessories', icon: 'mdi-watch', label: 'nav.accessories' },
];

export const accountLinks = [
  { to: '/user-profile', icon: 'mdi-account-outline', label: 'nav.profile' },
  { to: '/orders', icon: 'mdi-truck-outline', label: 'nav.orders' },
];

export const adminLinks = [
  { to: '/admin/users', icon: 'mdi-account-multiple-outline', label: 'nav.users' },
  { to: '/admin/create-user', icon: 'mdi-account-plus-outline', label: 'nav.createUser' },
  { to: '/admin/products', icon: 'mdi-package-variant-closed', label: 'nav.products' },
  { to: '/admin/create-product', icon: 'mdi-package-variant-plus', label: 'nav.createProduct' },
  { to: '/admin/settings', icon: 'mdi-cog-outline', label: 'nav.settings' },
];
