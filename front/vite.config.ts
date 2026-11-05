/// <reference types="vite-ssg" />
import { defineConfig } from 'vitest/config';
import vue from '@vitejs/plugin-vue';
import vuetify from 'vite-plugin-vuetify';
import { resolve } from 'path';

// The addresses of the products, to pre-render their pages: read from the API at build time. The build goes on
// with the other pages when the API does not answer (the product pages are then rendered by the browser only)
async function productPaths(): Promise<string[]> {
  const apiUrl = process.env.VITE_API_URL || 'http://localhost:8080';
  try {
    const response = await fetch(`${apiUrl}/api/products/`, { signal: AbortSignal.timeout(60_000) });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const products = (await response.json()) as { id: string }[];
    return products.map((product) => `/products/${encodeURIComponent(product.id)}`);
  } catch (error) {
    console.warn(`[prerender] products not read from ${apiUrl}, their pages are not pre-rendered:`, error);
    return [];
  }
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
      return [...publicPaths.map((route) => route.path), ...(await productPaths())];
    },
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
