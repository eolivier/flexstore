// The longest slug: the words of a long name are cut at a word, not in the middle
const MAX_SLUG_LENGTH = 60;

// The name of a product as a part of an address: lower case, no accent, words joined by hyphens
// ("T-shirt en coton bio !" -> "t-shirt-en-coton-bio")
export function slugOf(name: string): string {
  const words = name
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '');
  if (words.length <= MAX_SLUG_LENGTH) return words;
  const cut = words.slice(0, MAX_SLUG_LENGTH);
  return cut.slice(0, cut.lastIndexOf('-')) || cut;
}

// What the address of a product needs: its id, which finds it, and its name, which the search engines read
export interface ProductAddress {
  id?: string;
  name?: string;
}

// The one address of the page of a product: /products/<id>/<slug>. The id alone (/products/<id>) also finds the
// product, so that the addresses already shared keep working
export function productPath(product: ProductAddress): string {
  const slug = slugOf(product.name ?? '');
  return slug ? `/products/${product.id}/${slug}` : `/products/${product.id}`;
}
