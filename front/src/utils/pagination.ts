// The pages of a list: the number of items of a page when the front end asks no other size (the one of the
// back end), and the header that gives the number of items of the whole list
export const PAGE_SIZE = 20;
const TOTAL_COUNT_HEADER = 'X-Total-Count';

// The query string that asks for a page of a list; the pages are numbered from 0
export function pageQuery(page: number, size: number = PAGE_SIZE): string {
  return new URLSearchParams({ page: String(page), size: String(size) }).toString();
}

// The number of items of the whole list, given by the header of the answer; the items read when it is missing
export function totalCountOf(response: Response, itemsRead: number): number {
  const header = response.headers?.get(TOTAL_COUNT_HEADER);
  return header === null || header === undefined ? itemsRead : Number(header);
}

// A list that is empty still has its first page
export function pageCountOf(total: number, size: number = PAGE_SIZE): number {
  return Math.max(1, Math.ceil(total / size));
}
