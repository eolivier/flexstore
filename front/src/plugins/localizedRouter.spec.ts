import { describe, expect, it } from 'vitest';
import { createMemoryHistory, createRouter } from 'vue-router';
import { localizeRouter } from './localizedRouter';
import type { ShopLanguage } from '@/utils/language';

const Page = { render: () => null };

function routerSpeaking(language: ShopLanguage) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/about', component: Page },
      { path: '/en/about', component: Page },
      { path: '/cart', component: Page },
    ],
  });
  localizeRouter(router, () => language);
  return router;
}

describe('localizeRouter', () => {
  it('gives the link to a translated page the address of the language of the page', () => {
    // Arrange
    const router = routerSpeaking('en');
    // Act
    const link = router.resolve('/about?from=menu');
    // Assert
    expect(link.fullPath).toBe('/en/about?from=menu');
  });

  it('keeps the address of a page that is not translated', () => {
    // Arrange
    const router = routerSpeaking('en');
    // Act
    const link = router.resolve({ path: '/cart' });
    // Assert
    expect(link.path).toBe('/cart');
  });

  it('keeps an address that already says its language', () => {
    // Arrange
    const router = routerSpeaking('fr');
    // Act
    const link = router.resolve('/en/about');
    // Assert
    expect(link.path).toBe('/en/about');
  });

  it('navigates to the address of the language of the page', async () => {
    // Arrange
    const router = routerSpeaking('en');
    // Act
    await router.push('/about');
    // Assert
    expect(router.currentRoute.value.path).toBe('/en/about');
  });
});
