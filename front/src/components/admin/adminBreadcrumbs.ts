// A step of the trail: the label to translate, and the page it opens (none for the current page)
export interface AdminBreadcrumb {
  label: string;
  to?: string;
}

const home = { label: 'nav.admin', to: '/admin' };
const users = { label: 'nav.users', to: '/admin/users' };
const products = { label: 'nav.products', to: '/admin/products' };

// The trail from the home of the administration to the page; the sub-pages of the users and the products go
// through their list
const trails: { pattern: RegExp; parents: AdminBreadcrumb[]; label: string }[] = [
  { pattern: /^\/admin\/users\/[^/]+\/edit$/, parents: [home, users], label: 'editUser.title' },
  { pattern: /^\/admin\/create-user$/, parents: [home, users], label: 'nav.createUser' },
  { pattern: /^\/admin\/users$/, parents: [home], label: 'nav.users' },
  { pattern: /^\/admin\/products\/[^/]+\/edit$/, parents: [home, products], label: 'editProduct.title' },
  { pattern: /^\/admin\/create-product$/, parents: [home, products], label: 'nav.createProduct' },
  { pattern: /^\/admin\/products$/, parents: [home], label: 'nav.products' },
  { pattern: /^\/admin\/orders$/, parents: [home], label: 'nav.adminOrders' },
  { pattern: /^\/admin\/settings$/, parents: [home], label: 'nav.settings' },
  { pattern: /^\/admin$/, parents: [], label: 'nav.admin' },
];

export function adminBreadcrumbsOf(path: string): AdminBreadcrumb[] {
  const trail = trails.find(({ pattern }) => pattern.test(path.replace(/\/$/, '')));
  return trail ? [...trail.parents, { label: trail.label }] : [];
}
