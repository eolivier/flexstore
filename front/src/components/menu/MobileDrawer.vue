<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import Language from '../Language.vue';
  import BrandLogo from './BrandLogo.vue';
  import NavLinkItems from './NavLinkItems.vue';
  import ProductsSubMenu from './submenu/ProductsSubMenu.vue';
  import AdminSubMenu from './submenu/AdminSubMenu.vue';
  import { accountLinksOf } from './navigation';
  import CatalogSearchField from '@/components/products/CatalogSearchField.vue';

  const props = defineProps<{ modelValue: boolean; signedIn: boolean; admin: boolean }>();
  defineEmits<{ 'update:modelValue': [open: boolean] }>();
  const { t } = useI18n();

  const accountLinks = computed(() => accountLinksOf(props.signedIn));
</script>

<template>
  <v-navigation-drawer
    :model-value="modelValue"
    temporary
    width="290"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <BrandLogo class="pa-4" />
    <v-divider />
    <div class="pa-3">
      <CatalogSearchField />
    </div>
    <v-list nav>
      <v-list-item to="/" exact prepend-icon="mdi-home-outline" :title="t('nav.home')" />
      <ProductsSubMenu />
      <v-list-item to="/cart" prepend-icon="mdi-cart-outline" :title="t('nav.cart')" />
      <NavLinkItems :links="accountLinks" />
      <AdminSubMenu v-if="admin" />
      <v-list-item to="/about" prepend-icon="mdi-information-outline" :title="t('nav.about')" />
    </v-list>
    <template #append>
      <div class="pa-4 d-sm-none">
        <Language />
      </div>
    </template>
  </v-navigation-drawer>
</template>
