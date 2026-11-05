import { ViteSSG } from 'vite-ssg';
import './style.css';
import { createPinia } from 'pinia';
import vuetify from './plugins/vuetify';
import i18n from './plugins/i18n';
import App from './App.vue';
import { routes } from './routes';
import { SELLER_SETUP_PATH, sellerSetupIsPendingFor } from './utils/sellerSetupRedirect';
import { useAuthStore } from './stores/auth.store';
import { watch } from 'vue';
import { localizeRouter } from './plugins/localizedRouter';
import { isShopLanguage, languageOfPath, MAIN_LANGUAGE } from './utils/language';

// Pre-rendered at build time (npm run build): the pages are then shown again by the browser, not hydrated, so
// that a page served for another address (the shop answers every unknown address with its home page) is right
export const createApp = ViteSSG(
  App,
  { routes, base: import.meta.env.BASE_URL, scrollBehavior: () => ({ top: 0 }) },
  ({ app, router, isClient }) => {
    app.use(createPinia());
    app.use(vuetify);
    app.use(i18n);

    // The pages that exist in each language are in the language of their address; the others keep the one chosen
    router.beforeEach((to) => {
      if (to.meta.indexed) i18n.global.locale.value = languageOfPath(to.path);
    });
    localizeRouter(router, () => {
      const language = i18n.global.locale.value;
      return isShopLanguage(language) ? language : MAIN_LANGUAGE;
    });

    router.beforeEach(async (to) => {
      const authStore = useAuthStore();
      if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        return { path: '/login', query: { redirect: to.fullPath } };
      }
      if (to.meta.requiresAdmin && !authStore.isAdmin) {
        return { path: '/user-profile' };
      }
      if (to.meta.requiresAdmin && (await sellerSetupIsPendingFor(to.path))) {
        return { path: SELLER_SETUP_PATH };
      }
      return true;
    });

    if (isClient) {
      // Signed out (e.g. expired token) while on a protected page: back to the login
      watch(
        () => useAuthStore().isAuthenticated,
        (authenticated) => {
          const current = router.currentRoute.value;
          if (!authenticated && current.meta.requiresAuth) {
            router.push({ path: '/login', query: { redirect: current.fullPath } });
          }
        }
      );
    }
  }
);
