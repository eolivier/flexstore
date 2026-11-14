<script setup>
  import { ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRoute, useRouter } from 'vue-router';
  import { FullName } from '@/models/user';
  import Language from '../Language.vue';
  import ColorModeButton from '@/components/layout/ColorModeButton.vue';
  import LoadingBar from '@/components/layout/LoadingBar.vue';
  import CatalogSearchField from '@/components/products/CatalogSearchField.vue';
  import AccountMenu from './AccountMenu.vue';
  import BrandLogo from './BrandLogo.vue';
  import HeaderCartButton from './HeaderCartButton.vue';
  import HeaderNavLinks from './HeaderNavLinks.vue';
  import LowStockBell from './LowStockBell.vue';
  import MobileDrawer from './MobileDrawer.vue';
  import { useAuthStore } from '@/stores/auth.store';
  import { useHeaderCart } from '@/composables/useHeaderCart';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const authStore = useAuthStore();
  const { cartCount } = useHeaderCart();

  const drawer = ref(false);

  watch(
    () => route.fullPath,
    () => {
      drawer.value = false;
    }
  );

  function logout() {
    authStore.logout();
    router.push('/');
  }
</script>

<template>
  <v-app-bar flat border="b" color="surface" height="72">
    <LoadingBar />
    <v-container class="d-flex align-center py-0">
      <v-app-bar-nav-icon class="d-md-none mr-1" :aria-label="t('nav.openMenu')" @click="drawer = !drawer" />
      <router-link to="/" class="d-flex text-decoration-none mr-8">
        <BrandLogo />
      </router-link>
      <HeaderNavLinks />
      <v-spacer />
      <div class="d-none d-md-flex flex-grow-1 justify-end mr-2">
        <CatalogSearchField />
      </div>
      <div class="d-none d-sm-flex">
        <Language />
      </div>
      <ColorModeButton />
      <LowStockBell v-if="authStore.isAdmin" />
      <AccountMenu
        :signed-in="authStore.isAuthenticated"
        :admin="authStore.isAdmin"
        :full-name="FullName.of(authStore.currentUser).value"
        :email="authStore.currentUser?.email ?? ''"
        @logout="logout"
      />
      <HeaderCartButton :count="cartCount" />
    </v-container>
  </v-app-bar>

  <MobileDrawer v-model="drawer" :signed-in="authStore.isAuthenticated" :admin="authStore.isAdmin" />
</template>
