<script setup lang="ts">
  import { computed, onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useProductsStore } from '@/stores/products.store.ts';
  import { categoryInfo } from '@/components/products/categories';
  import { formatPrice } from '@/utils/price';

  const productsStore = useProductsStore();
  const { t, locale } = useI18n();

  onMounted(() => {
    productsStore.fetchProducts();
  });

  const headers = computed(() => [
    { title: t('table.name'), key: 'name' },
    { title: t('table.category'), key: 'category' },
    { title: t('table.price'), key: 'price', align: 'end' as const },
    { title: '', key: 'actions', sortable: false, align: 'end' as const },
  ]);
</script>

<template>
  <v-container class="py-10">
    <div class="d-flex flex-wrap align-end ga-4 mb-6">
      <div>
        <h1 class="text-h4 font-weight-bold">{{ t('adminProducts.title') }}</h1>
        <p class="text-body-2 text-medium-emphasis mt-1">{{ t('adminProducts.count', productsStore.products.length) }}</p>
      </div>
      <v-spacer />
      <v-btn to="/admin/create-product" color="primary" prepend-icon="mdi-package-variant-plus">{{ t('nav.createProduct') }}</v-btn>
    </div>

    <v-data-table
      :headers="headers"
      :items="productsStore.products"
      :loading="productsStore.loading"
      :loading-text="t('adminProducts.loading')"
      item-value="id"
    >
      <template #item.name="{ item }">
        <div class="py-2">
          <div class="font-weight-medium">{{ item.name }}</div>
          <div class="text-caption text-medium-emphasis">{{ item.description }}</div>
        </div>
      </template>
      <template #item.category="{ item }">
        <v-chip :prepend-icon="categoryInfo(item.category).icon" variant="tonal" size="small">
          {{ t(categoryInfo(item.category).label) }}
        </v-chip>
      </template>
      <template #item.price="{ item }">{{ formatPrice(item.price, item.currency, locale) }}</template>
      <template #item.actions="{ item }">
        <v-btn
          :to="`/admin/products/${item.id}/edit`"
          icon="mdi-pencil-outline"
          variant="text"
          size="small"
          :aria-label="t('adminProducts.edit', { name: item.name })"
        />
      </template>
    </v-data-table>
  </v-container>
</template>
