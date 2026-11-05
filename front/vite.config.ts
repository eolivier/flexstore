/// <reference types="vite-ssg" />
import { defineConfig } from 'vitest/config';
import vue from '@vitejs/plugin-vue';
import vuetify from 'vite-plugin-vuetify';
import { resolve } from 'path';
import { readFileSync, writeFileSync } from 'fs';
import { productPath } from './src/utils/productSlug';
import { robotsTxtWithSitemap, sitemapXml } from './src/utils/sitemap';

// The language of the pre-rendered pages: the default one of the shop, the one of the slugs of the products
const PRERENDER_LANGUAGE = 'fr';

// The addresses of the products, to pre-render their pages: read from the API at build time. The build goes on
// with the other pages when the API does not answer (the product pages are then rendered by the browser only)
async function productPaths(): Promise<string[]> {
  const apiUrl = process.env.VITE_API_URL || 'http://localhost:8080';
  try {
    const response = await fetch(`${apiUrl}/api/products/`, {
      headers: { 'Accept-Language': PRERENDER_LANGUAGE },
      signal: AbortSignal.timeout(60_000),
    });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const products = (await response.json()) as { id: string; name: string }[];
    return products.map((product) => productPath(product));
  } catch (error) {
    console.warn(`[prerender] products not read from ${apiUrl}, their pages are not pre-rendered:`, error);
    return [];
  }
}

// The pages that are pre-rendered: the sitemap lists them
let prerenderedPaths: string[] = [];

// The sitemap and robots.txt need the absolute address of the shop (VITE_SITE_URL): without it, no sitemap
function writeSitemap() {
  const shopUrl = (process.env.VITE_SITE_URL ?? '').replace(/\/+$/, '');
  if (!shopUrl) {
    console.warn('[sitemap] VITE_SITE_URL is not set: sitemap.xml is not written');
    return;
  }
  const outputDirectory = resolve(__dirname, 'dist');
  writeFileSync(resolve(outputDirectory, 'sitemap.xml'), sitemapXml(shopUrl, prerenderedPaths));
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
      const publicPaths = routes.filter((route) => route.meta?.indexed && !route.path.includes(':'));
      prerenderedPaths = [...publicPaths.map((route) => route.path), ...(await productPaths())];
      return prerenderedPaths;
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
