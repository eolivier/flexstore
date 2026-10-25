// Opens a page outside the application (the checkout page of the payment provider)
export function openExternalPage(url: string) {
  window.location.assign(url);
}
