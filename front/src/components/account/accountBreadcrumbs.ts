// A step of the trail: the label to translate, and the page it opens (none for the current page)
export interface AccountBreadcrumb {
  label: string;
  to?: string;
}

const home = { label: 'nav.account', to: '/user-profile' };
const orders = { label: 'nav.orders', to: '/orders' };

// The trail from the home of the customer's own space to the page; the order pages go through their list
const trails: { pattern: RegExp; parents: AccountBreadcrumb[]; label: string }[] = [
  { pattern: /^\/account\/addresses$/, parents: [home], label: 'addressBook.title' },
  { pattern: /^\/account\/preferences$/, parents: [home], label: 'nav.preferences' },
  { pattern: /^\/account\/loyalty$/, parents: [home], label: 'nav.loyalty' },
  { pattern: /^\/orders\/[^/]+\/confirmation$/, parents: [home, orders], label: 'orders.detail' },
  { pattern: /^\/orders\/[^/]+$/, parents: [home, orders], label: 'orders.detail' },
  { pattern: /^\/orders$/, parents: [home], label: 'nav.orders' },
  { pattern: /^\/user-profile$/, parents: [], label: 'nav.account' },
];

export function accountBreadcrumbsOf(path: string): AccountBreadcrumb[] {
  const trail = trails.find(({ pattern }) => pattern.test(path.replace(/\/$/, '')));
  return trail ? [...trail.parents, { label: trail.label }] : [];
}
