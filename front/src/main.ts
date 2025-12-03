import { createApp } from 'vue';
import './style.css';
import { createPinia } from 'pinia';
import { createVuetify } from 'vuetify';
import * as components from 'vuetify/components';
import * as directives from 'vuetify/directives';

import 'vuetify/styles';
import '@mdi/font/css/materialdesignicons.css';
import { createI18n } from 'vue-i18n';
import App from './App.vue';
import { createRouter, createWebHistory } from 'vue-router';

import Home from './components/Home.vue';
import Users from './components/admin/Users.vue';
import Cart from './components/Cart.vue';
import About from './components/About.vue';
import CreateUser from './components/admin/CreateUser.vue';
import Accessories from './components/products/Accessories.vue';
import Clothing from './components/products/Clothing.vue';
import Electronics from './components/products/Electronics.vue';
import AdminSubMenu from './components/menu/submenu/AdminSubMenu.vue';
import UserProfile from './components/UserProfile.vue';
import Orders from './components/Orders.vue';
import Products from "./components/products/Products.vue";
import CreateProduct from './components/admin/CreateProduct.vue';
import Login from './components/Login.vue';
import messages from './i18n/messages';
import { useAuthStore } from './stores/auth.store';

const vuetify = createVuetify({ components, directives });
const pinia = createPinia();

const routes = [
  { path: '/', component: Home },
  { path: '/cart', component: Cart },
  { path: '/orders', component: Orders },
  { path: '/user-profile', component: UserProfile },
  { path: '/products/', component: Products },
  { path: '/products/clothing', component: Clothing },
  { path: '/products/accessories', component: Accessories },
  { path: '/products/electronics', component: Electronics },
  { path: '/login', component: Login },
  { path: '/admin', component: AdminSubMenu, meta: { requiresAuth: true } },
  { path: '/admin/users', component: Users, meta: { requiresAuth: true } },
  { path: '/admin/create-user', component: CreateUser, meta: { requiresAuth: true } },
  { path: '/admin/create-product', component: CreateProduct, meta: { requiresAuth: true } },
  { path: '/about', component: About },
];
const router = createRouter({
  history: createWebHistory(),
  routes,
});

const i18n = createI18n({
  locale: 'fr', // ou 'en'
  messages,
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
  } else {
    next();
  }
});

app.mount('#app');
