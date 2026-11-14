import { describe, expect, it } from 'vitest';
import { absoluteUrl } from './pageAddress';

const shopUrl = 'https://shop.example';

describe('pageAddress', () => {
  it.each([
    { path: '/products', expected: 'https://shop.example/products' },
    { path: '/products/', expected: 'https://shop.example/products' },
    { path: '/products///', expected: 'https://shop.example/products' },
    { path: '/products/?q=jeans#top', expected: 'https://shop.example/products' },
    { path: '/products#top', expected: 'https://shop.example/products' },
    { path: '/products/p-1?lang=fr', expected: 'https://shop.example/products/p-1' },
  ])('the path "$path" should be the address $expected', ({ path, expected }) => {
    // Act
    const address = absoluteUrl(shopUrl, path);
    // Assert
    expect(address).toBe(expected);
  });

  it.each(['/', '', '/?q=jeans', '#top', '//'])('the path "%s" should be the home page', (path) => {
    // Act
    const address = absoluteUrl(shopUrl, path);
    // Assert
    expect(address).toBe('https://shop.example/');
  });
});
