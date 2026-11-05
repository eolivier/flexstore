/// <reference types="vite-ssg" />
import { defineConfig } from 'vitest/config';
import vue from '@vitejs/plugin-vue';
import vuetify from 'vite-plugin-vuetify';
import { resolve } from 'path';
import { readFileSync, writeFileSync } from 'fs';
import { productPath } from './src/utils/productSlug';
import { robotsTxtWithSitemap, sitemapXml, type SitemapPage } from './src/utils/sitemap';
import { languageOfPath, localizedPath, type ShopLanguage } from './src/utils/language';

// The products in a language, read from the API at build time
async function productsIn(language: ShopLanguage): Promise<{ id: string; name: string }[]> {
  const apiUrl = process.env.VITE_API_URL || 'http://localhost:8080';
  const response = await fetch(`${apiUrl}/api/products/`, {
    headers: { 'Accept-Language': language },
    signal: AbortSignal.timeout(60_000),
  });
  if (!response.ok) throw new Error(`HTTP ${response.status}`);
  return (await response.json()) as { id: string; name: string }[];
}

// The pages of the products, in each language (the slug is made from the name, which is translated). The build
// goes on with the other pages when the API does not answer (the product pages are then rendered by the browser)
async function productPages(): Promise<SitemapPage[]> {
  try {
    const [frenchProducts, englishProducts] = await Promise.all([productsIn('fr'), productsIn('en')]);
    return frenchProducts.map((frenchProduct) => {
      const englishProduct = englishProducts.find((product) => product.id === frenchProduct.id);
      return {
        paths: {
          fr: productPath(frenchProduct),
          en: localizedPath(productPath(englishProduct ?? frenchProduct), 'en'),
        },
      };
    });
  } catch (error) {
    console.warn('[prerender] products not read from the API, their pages are not pre-rendered:', error);
    return [];
  }
}

// The pages that are pre-rendered: the sitemap lists them
let prerenderedPages: SitemapPage[] = [];

// The sitemap and robots.txt need the absolute address of the shop (VITE_SITE_URL): without it, no sitemap
function writeSitemap() {
  const shopUrl = (process.env.VITE_SITE_URL ?? '').replace(/\/+$/, '');
  if (!shopUrl) {
    console.warn('[sitemap] VITE_SITE_URL is not set: sitemap.xml is not written');
    return;
  }
  const outputDirectory = resolve(__dirname, 'dist');
  writeFileSync(resolve(outputDirectory, 'sitemap.xml'), sitemapXml(shopUrl, prerenderedPages));
  const robotsTxt = readFileSync(resolve(outputDirectory, 'robots.txt'), 'utf-8');
  writeFileSync(resolve(outputDirectory, 'robots.txt'), robotsTxtWithSitemap(robotsTxt, shopUrl));
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vuetify()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  ssr: {
    noExternal: ['vuetify'],
  },
  // vite-ssg pre-renders the public pages (the routes that say indexed) when the app is built
  ssgOptions: {
    script: 'async',
    formatting: 'none',
    async includedRoutes(_paths, routes) {
      const staticPages = routes
        .filter((route) => route.meta?.indexed && !route.path.includes(':'))
        .filter((route) => languageOfPath(route.path) === 'fr')
        .map((route) => ({ paths: { fr: route.path, en: localizedPath(route.path, 'en') } }));
      prerenderedPages = [...staticPages, ...(await productPages())];
      return prerenderedPages.flatMap((page) => [page.paths.fr, page.paths.en]);
    },
    onFinished: writeSitemap,
  },
  test: {
    environment: 'happy-dom',
    globals: true,
    include: ['src/**/*.{test,spec}.{js,ts,vue}'],
    setupFiles: ['src/test/setup.ts'],
    css: true,
    server: {
      deps: {
        inline: ['vuetify'],
      },
    },
  },
});
