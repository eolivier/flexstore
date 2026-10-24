import { createApp, watch } from 'vue';
import './style.css';
import { createPinia } from 'pinia';
import vuetify from './plugins/vuetify';
import i18n from './plugins/i18n';
import App from './App.vue';
import { createRouter, createWebHistory } from 'vue-router';

import Home from './components/Home.vue';
import Users from './components/admin/Users.vue';
import Cart from './components/Cart.vue';
import About from './components/About.vue';
import CreateUser from './components/admin/CreateUser.vue';
import AdminHome from './components/admin/AdminHome.vue';
import UserProfile from './components/UserProfile.vue';
import Orders from './components/Orders.vue';
import Products from "./components/products/Products.vue";
import ProductDetail from './components/products/ProductDetail.vue';
import CreateProduct from './components/admin/CreateProduct.vue';
import Login from './components/Login.vue';
import { useAuthStore } from './stores/auth.store';

const pinia = createPinia();

const routes = [
  { path: '/', component: Home },
  { path: '/cart', component: Cart },
  { path: '/orders', component: Orders, meta: { requiresAuth: true } },
  { path: '/user-profile', component: UserProfile },
  { path: '/products/', component: Products },
  { path: '/products/clothing', component: Products, props: { category: 'CLOTHING' } },
  { path: '/products/accessories', component: Products, props: { category: 'ACCESSORIES' } },
  { path: '/products/electronics', component: Products, props: { category: 'ELECTRONICS' } },
  { path: '/products/:id', component: ProductDetail, props: true },
  { path: '/login', component: Login },
  { path: '/admin', component: AdminHome, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/users', component: Users, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/create-user', component: CreateUser, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/create-product', component: CreateProduct, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/about', component: About },
];
const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
});

const app = createApp(App);
app.use(pinia);
app.use(vuetify);
app.use(router);
app.use(i18n);

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore();
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ path: '/login', query: { redirect: to.fullPath } });
  } else if (to.meta.requiresAdmin && !authStore.isAdmin) {
    next({ path: '/user-profile' });
  } else {
    next();
  }
});

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

app.mount('#app');
