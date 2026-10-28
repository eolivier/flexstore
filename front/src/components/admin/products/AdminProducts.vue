<script setup lang="ts">
  import AdminBreadcrumbs from '@/components/admin/AdminBreadcrumbs.vue';
  import { computed, onMounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useProductsStore } from '@/stores/products.store.ts';
  import { useStocksStore } from '@/stores/stocks.store';
  import { categoryInfo } from '@/components/products/categories';
  import { formatPrice } from '@/utils/price';
  import { stockStateOf } from '@/utils/stock';
  import { matchesFilter, noFilter, type ProductFilter } from '@/utils/productFilter';
  import AdminProductFilters from './AdminProductFilters.vue';

  const productsStore = useProductsStore();
  const stocksStore = useStocksStore();
  const { t, locale } = useI18n();

  onMounted(() => {
    productsStore.fetchProducts();
    stocksStore.fetchStocks();
  });

  const filter = ref<ProductFilter>({ ...noFilter });
  const filteredProducts = computed(() =>
    productsStore.products.filter((product) =>
      matchesFilter(product, stocksStore.quantityOf(product.id), filter.value)
    )
  );

  // Out of stock in red, a few units left in orange
  const stockColors = { outOfStock: 'error', fewLeft: 'warning', inStock: undefined };
  function stockColor(quantity: number | null) {
    return quantity === null ? undefined : stockColors[stockStateOf(quantity)];
  }

  const headers = computed(() => [
    { title: t('table.name'), key: 'name' },
    { title: t('table.category'), key: 'category' },
    { title: t('table.price'), key: 'price', align: 'end' as const },
    {
      title: t('table.stock'),
      key: 'stock',
      align: 'end' as const,
      value: (product: { id?: string }) => stocksStore.quantityOf(product.id) ?? 0,
    },
    { title: '', key: 'actions', sortable: false, align: 'end' as const },
  ]);
</script>

<template>
  <v-container class="py-10">
    <AdminBreadcrumbs />
    <div class="d-flex flex-wrap align-end ga-4 mb-6">
      <div>
        <h1 class="text-h4 font-weight-bold">{{ t('adminProducts.title') }}</h1>
        <p class="text-body-2 text-medium-emphasis mt-1">
          {{ t('adminProducts.count', filteredProducts.length) }}
        </p>
      </div>
      <v-spacer />
      <v-btn to="/admin/create-product" color="primary" prepend-icon="mdi-package-variant-plus">{{
        t('nav.createProduct')
      }}</v-btn>
    </div>

    <AdminProductFilters v-model="filter" />

    <v-data-table
      :headers="headers"
      :items="filteredProducts"
      :loading="productsStore.loading"
      :loading-text="t('adminProducts.loading')"
      item-value="id"
      :no-data-text="t('adminProducts.noMatch')"
    >
      <template #item.name="{ item }">
        <div class="py-2">
          <div class="font-weight-medium">{{ item.name }}</div>
          <div class="text-caption text-medium-emphasis">{{ t('sku.label') }} {{ item.sku }}</div>
          <div class="text-caption text-medium-emphasis">{{ item.description }}</div>
        </div>
      </template>
      <template #item.category="{ item }">
        <v-chip :prepend-icon="categoryInfo(item.category).icon" variant="tonal" size="small">
          {{ t(categoryInfo(item.category).label) }}
        </v-chip>
      </template>
      <template #item.price="{ item }">{{ formatPrice(item.price, item.currency, locale) }}</template>
      <template #item.stock="{ item }">
        <v-chip
          v-if="stocksStore.quantityOf(item.id) !== null"
          :color="stockColor(stocksStore.quantityOf(item.id))"
          variant="tonal"
          size="small"
        >
          {{ stocksStore.quantityOf(item.id) }}
        </v-chip>
      </template>
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
