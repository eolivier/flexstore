import 'vue-router';

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean;
    requiresAdmin?: boolean;
    // The message keys of the title and of the description of the page
    title?: string;
    description?: string;
    // The search engines may list the page: only the public pages say so
    indexed?: boolean;
  }
}
