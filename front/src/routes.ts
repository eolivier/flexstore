import type { RouteRecordRaw } from 'vue-router';

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
import Wishlist from './components/account/wishlist/Wishlist.vue';
import ShopSettings from './components/admin/settings/ShopSettings.vue';
import SalesDashboard from './components/admin/sales/SalesDashboard.vue';
import SellerSettings from './components/admin/seller/SellerSettings.vue';
import SellerSetupWizard from './components/admin/seller/setup/SellerSetupWizard.vue';
import { SELLER_SETUP_PATH } from './utils/sellerSetupRedirect';
import AdminLegalPages from './components/admin/legal/AdminLegalPages.vue';
import LegalPage from './components/legal/LegalPage.vue';
import AdminDiscountCodes from './components/admin/discounts/AdminDiscountCodes.vue';
import Login from './components/Login.vue';
import SignUp from './components/account/SignUp.vue';
import VerifyEmail from './components/account/VerifyEmail.vue';
import ForgotPassword from './components/account/ForgotPassword.vue';
import ResetPassword from './components/account/ResetPassword.vue';
import Checkout from './components/checkout/Checkout.vue';

// The pages of the shop. indexed: the public pages, which are pre-rendered at build time and listed by the
// search engines
export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: Home,
    meta: { title: 'seo.homeTitle', description: 'seo.siteDescription', indexed: true },
  },
  { path: '/cart', component: Cart },
  { path: '/checkout', component: Checkout, meta: { requiresAuth: true } },
  { path: '/orders', component: Orders, meta: { requiresAuth: true } },
  { path: '/account/addresses', component: AddressBook, meta: { requiresAuth: true } },
  { path: '/account/wishlist', component: Wishlist, meta: { requiresAuth: true } },
  { path: '/orders/:id', component: OrderDetail, props: true, meta: { requiresAuth: true } },
  {
    path: '/orders/:id/confirmation',
    component: OrderConfirmation,
    props: true,
    meta: { requiresAuth: true },
  },
  { path: '/user-profile', component: UserProfile },
  { path: '/products/', component: Products, meta: { indexed: true } },
  { path: '/products/clothing', component: Products, props: { category: 'CLOTHING' }, meta: { indexed: true } },
  {
    path: '/products/accessories',
    component: Products,
    props: { category: 'ACCESSORIES' },
    meta: { indexed: true },
  },
  {
    path: '/products/electronics',
    component: Products,
    props: { category: 'ELECTRONICS' },
    meta: { indexed: true },
  },
  {
    path: '/products/:id/:slug?',
    component: ProductDetail,
    props: (route) => ({ id: route.params.id }),
    meta: { indexed: true },
  },
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
    path: '/admin/sales',
    component: SalesDashboard,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/seller',
    component: SellerSettings,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: SELLER_SETUP_PATH,
    component: SellerSetupWizard,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/legal-pages',
    component: AdminLegalPages,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/settings',
    component: ShopSettings,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/about',
    component: About,
    meta: { title: 'nav.about', description: 'seo.about', indexed: true },
  },
  {
    path: '/legal-notice',
    component: LegalPage,
    props: { kind: 'legal-notice' },
    meta: { title: 'nav.legalNotice', description: 'seo.legalNotice', indexed: true },
  },
  {
    path: '/terms-of-sale',
    component: LegalPage,
    props: { kind: 'terms-of-sale' },
    meta: { title: 'nav.termsOfSale', description: 'seo.termsOfSale', indexed: true },
  },
];
