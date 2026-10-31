import { createApp, watch } from 'vue';
import './style.css';
import { createPinia } from 'pinia';
import vuetify from './plugins/vuetify';
import i18n from './plugins/i18n';
import App from './App.vue';
import { createRouter, createWebHistory } from 'vue-router';

import Home from './components/Home.vue';
import Users from './components/admin/Users.vue';
import EditUser from './components/admin/EditUser.vue';
import Cart from './components/Cart.vue';
import About from './components/About.vue';
import CreateUser from './components/admin/CreateUser.vue';
import AdminHome from './components/admin/AdminHome.vue';
import UserProfile from './components/UserProfile.vue';
import Orders from './components/orders/Orders.vue';
import OrderDetail from './components/orders/OrderDetail.vue';
import OrderConfirmation from './components/orders/OrderConfirmation.vue';
import Products from './components/products/Products.vue';
import ProductDetail from './components/products/ProductDetail.vue';
import CreateProduct from './components/admin/products/CreateProduct.vue';
import AdminProducts from './components/admin/products/AdminProducts.vue';
import EditProduct from './components/admin/products/EditProduct.vue';
import AdminOrders from './components/admin/orders/AdminOrders.vue';
import AdminOrderDetail from './components/admin/orders/AdminOrderDetail.vue';
import AddressBook from './components/account/addresses/AddressBook.vue';
import ShopSettings from './components/admin/settings/ShopSettings.vue';
import AdminDiscountCodes from './components/admin/discounts/AdminDiscountCodes.vue';
import Login from './components/Login.vue';
import SignUp from './components/account/SignUp.vue';
import VerifyEmail from './components/account/VerifyEmail.vue';
import ForgotPassword from './components/account/ForgotPassword.vue';
import ResetPassword from './components/account/ResetPassword.vue';
import Checkout from './components/checkout/Checkout.vue';
import { useAuthStore } from './stores/auth.store';

const pinia = createPinia();

const routes = [
  { path: '/', component: Home },
  { path: '/cart', component: Cart },
  { path: '/checkout', component: Checkout, meta: { requiresAuth: true } },
  { path: '/orders', component: Orders, meta: { requiresAuth: true } },
  { path: '/account/addresses', component: AddressBook, meta: { requiresAuth: true } },
  { path: '/orders/:id', component: OrderDetail, props: true, meta: { requiresAuth: true } },
  {
    path: '/orders/:id/confirmation',
    component: OrderConfirmation,
    props: true,
    meta: { requiresAuth: true },
  },
  { path: '/user-profile', component: UserProfile },
  { path: '/products/', component: Products },
  { path: '/products/clothing', component: Products, props: { category: 'CLOTHING' } },
  { path: '/products/accessories', component: Products, props: { category: 'ACCESSORIES' } },
  { path: '/products/electronics', component: Products, props: { category: 'ELECTRONICS' } },
  { path: '/products/:id', component: ProductDetail, props: true },
  { path: '/login', component: Login },
  { path: '/sign-up', component: SignUp },
  { path: '/verify-email', component: VerifyEmail },
  { path: '/forgot-password', component: ForgotPassword },
  { path: '/reset-password', component: ResetPassword },
  { path: '/admin', component: AdminHome, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/users', component: Users, meta: { requiresAuth: true, requiresAdmin: true } },
  {
    path: '/admin/users/:id/edit',
    component: EditUser,
    props: true,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/create-user',
    component: CreateUser,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/products',
    component: AdminProducts,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/orders',
    component: AdminOrders,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/orders/:id',
    component: AdminOrderDetail,
    props: true,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/products/:id/edit',
    component: EditProduct,
    props: true,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/create-product',
    component: CreateProduct,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/discount-codes',
    component: AdminDiscountCodes,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/settings',
    component: ShopSettings,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
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
