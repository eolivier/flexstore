<script setup>
  import { computed, onMounted, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRoute, useRouter } from 'vue-router';
  import ProductsSubMenu from './submenu/ProductsSubMenu.vue';
  import AdminSubMenu from './submenu/AdminSubMenu.vue';
  import Language from '../Language.vue';
  import { accountLinks, adminLinks, shopLinks } from './navigation';
  import { useAuthStore } from '@/stores/auth.store';
  import { useCartStore } from '@/stores/cart.store';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const authStore = useAuthStore();
  const cartStore = useCartStore();

  const drawer = ref(false);

  const cartCount = computed(() =>
    (cartStore.cartItems?.items ?? []).reduce((count, item) => count + item.productQuantity, 0)
  );

  watch(() => route.fullPath, () => {
    drawer.value = false;
  });

  onMounted(() => {
    cartStore.fetchCartItems();
  });

  function logout() {
    authStore.logout();
    router.push('/');
  }
</script>

<template>
  <v-app-bar flat border="b" color="surface" height="72">
    <div class="header-inner d-flex align-center w-100 px-4">
      <v-app-bar-nav-icon
        class="d-md-none mr-1"
        :aria-label="t('nav.openMenu')"
        @click="drawer = !drawer"
      />

      <router-link to="/" class="brand d-flex align-center text-decoration-none mr-8">
        <span class="brand-mark d-flex align-center justify-center mr-2">
          <v-icon icon="mdi-shopping" size="20" color="white" />
        </span>
        <span class="text-h6 font-weight-bold text-primary">Flexstore</span>
      </router-link>

      <nav class="d-none d-md-flex align-center ga-1">
        <v-btn to="/" variant="text" exact>{{ t('nav.home') }}</v-btn>
        <v-menu open-on-hover location="bottom start">
          <template #activator="{ props }">
            <v-btn v-bind="props" variant="text" append-icon="mdi-chevron-down">
              {{ t('nav.shop') }}
            </v-btn>
          </template>
          <v-list density="comfortable" min-width="220">
            <v-list-item
              v-for="link in shopLinks"
              :key="link.to"
              :to="link.to"
              :prepend-icon="link.icon"
              :title="t(link.label)"
            />
          </v-list>
        </v-menu>
        <v-btn to="/about" variant="text">{{ t('nav.about') }}</v-btn>
      </nav>

      <v-spacer />

      <div class="d-none d-sm-flex">
        <Language />
      </div>

      <v-menu location="bottom end">
        <template #activator="{ props }">
          <v-btn v-bind="props" icon="mdi-account-circle-outline" variant="text" :aria-label="t('nav.account')" />
        </template>
        <v-list density="comfortable" min-width="240">
          <v-list-item v-if="authStore.isAuthenticated" :subtitle="authStore.currentUser?.email">
            <v-list-item-title class="font-weight-bold">{{ authStore.currentUser?.name }}</v-list-item-title>
          </v-list-item>
          <v-divider v-if="authStore.isAuthenticated" class="my-1" />
          <v-list-item
            v-for="link in accountLinks"
            :key="link.to"
            :to="link.to"
            :prepend-icon="link.icon"
            :title="t(link.label)"
          />
          <template v-if="authStore.isAdmin">
            <v-divider class="my-1" />
            <v-list-subheader>{{ t('nav.admin') }}</v-list-subheader>
            <v-list-item
              v-for="link in adminLinks"
              :key="link.to"
              :to="link.to"
              :prepend-icon="link.icon"
              :title="t(link.label)"
            />
          </template>
          <v-divider class="my-1" />
          <v-list-item
            v-if="authStore.isAuthenticated"
            prepend-icon="mdi-logout"
            :title="t('nav.logout')"
            @click="logout"
          />
          <v-list-item v-else to="/login" prepend-icon="mdi-login" :title="t('nav.login')" />
        </v-list>
      </v-menu>

      <v-btn to="/cart" icon variant="text" :aria-label="t('nav.cart')">
        <v-badge :content="cartCount" :model-value="cartCount > 0" color="secondary">
          <v-icon icon="mdi-cart-outline" />
        </v-badge>
      </v-btn>
    </div>
  </v-app-bar>

  <v-navigation-drawer v-model="drawer" temporary width="290">
    <div class="d-flex align-center pa-4">
      <span class="brand-mark d-flex align-center justify-center mr-2">
        <v-icon icon="mdi-shopping" size="20" color="white" />
      </span>
      <span class="text-h6 font-weight-bold text-primary">Flexstore</span>
    </div>
    <v-divider />
    <v-list nav>
      <v-list-item to="/" exact prepend-icon="mdi-home-outline" :title="t('nav.home')" />
      <ProductsSubMenu />
      <v-list-item to="/cart" prepend-icon="mdi-cart-outline" :title="t('nav.cart')" />
      <v-list-item
        v-for="link in accountLinks"
        :key="link.to"
        :to="link.to"
        :prepend-icon="link.icon"
        :title="t(link.label)"
      />
      <AdminSubMenu v-if="authStore.isAdmin" />
      <v-list-item to="/about" prepend-icon="mdi-information-outline" :title="t('nav.about')" />
    </v-list>
    <template #append>
      <div class="pa-4 d-sm-none">
        <Language />
      </div>
    </template>
  </v-navigation-drawer>

  <v-snackbar v-model="cartStore.accountItemsMerged" color="primary" timeout="5000" location="bottom right">
    <v-icon icon="mdi-cart-check" class="mr-2" />
    {{ t('cart.accountItemsMerged') }}
    <template #actions>
      <v-btn to="/cart" variant="text" color="secondary">{{ t('products.viewCart') }}</v-btn>
    </template>
  </v-snackbar>
</template>

<style scoped>
  .header-inner {
    max-width: 1200px;
    margin: 0 auto;
  }
  .brand-mark {
    width: 34px;
    height: 34px;
    border-radius: 10px;
    background: rgb(var(--v-theme-secondary));
  }
</style>
