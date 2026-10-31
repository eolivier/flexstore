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
  { to: '/account/addresses', icon: 'mdi-map-marker-outline', label: 'nav.addresses' },
];

// The links of the account only make sense once signed in
export function accountLinksOf(signedIn: boolean) {
  return signedIn ? accountLinks : [];
}

export const adminLinks = [
  { to: '/admin/sales', icon: 'mdi-chart-line', label: 'nav.salesDashboard' },
  { to: '/admin/users', icon: 'mdi-account-multiple-outline', label: 'nav.users' },
  { to: '/admin/create-user', icon: 'mdi-account-plus-outline', label: 'nav.createUser' },
  { to: '/admin/products', icon: 'mdi-package-variant-closed', label: 'nav.products' },
  { to: '/admin/create-product', icon: 'mdi-package-variant-plus', label: 'nav.createProduct' },
  { to: '/admin/orders', icon: 'mdi-truck-delivery-outline', label: 'nav.adminOrders' },
  { to: '/admin/discount-codes', icon: 'mdi-ticket-percent-outline', label: 'nav.discountCodes' },
  { to: '/admin/seller', icon: 'mdi-store-outline', label: 'nav.seller' },
  { to: '/admin/settings', icon: 'mdi-cog-outline', label: 'nav.settings' },
];
