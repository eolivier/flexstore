import { describe, expect, it } from 'vitest';
import { PAGE_SIZE, pageCountOf, pageQuery, totalCountOf } from './pagination';

const responseWith = (headers: Record<string, string>) => new Response('[]', { headers });

describe('pagination', () => {
  it('should ask a page with its number from 0 and the default size', () => {
    // Act
    const query = pageQuery(2);
    // Assert
    expect(query).toBe(`page=2&size=${PAGE_SIZE}`);
  });

  it('should read the number of items of the whole list in the header', () => {
    // Act
    const total = totalCountOf(responseWith({ 'X-Total-Count': '57' }), 20);
    // Assert
    expect(total).toBe(57);
  });

  it('should count the items read when the header is missing', () => {
    // Act
    const total = totalCountOf(responseWith({}), 3);
    // Assert
    expect(total).toBe(3);
  });

  it.each([
    { total: 0, pages: 1 },
    { total: 1, pages: 1 },
    { total: 20, pages: 1 },
    { total: 21, pages: 2 },
    { total: 100, pages: 5 },
  ])('a list of $total items should have $pages pages', ({ total, pages }) => {
    // Act
    const pageCount = pageCountOf(total);
    // Assert
    expect(pageCount).toBe(pages);
  });
});
