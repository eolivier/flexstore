// The address of a page under the address of the shop: no query, no fragment, no final slash
// (https://shop.example + /products/?q=jeans#top -> https://shop.example/products)
export function absoluteUrl(shopUrl: string, path: string): string {
  const withoutQuery = path.split(/[?#]/)[0];
  const withoutFinalSlash = withoutQuery.replace(/\/+$/, '');
  return shopUrl + (withoutFinalSlash || '/');
}
