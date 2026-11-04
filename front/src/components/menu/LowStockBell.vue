<script setup lang="ts">
  import { computed, onMounted, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRoute } from 'vue-router';
  import { useStockAlertsStore } from '@/stores/stockAlerts.store';
  import { useProductsStore } from '@/stores/products.store';

  // The products that run low, for the administrators: read again at each page they open
  const { t } = useI18n();
  const route = useRoute();
  const stockAlertsStore = useStockAlertsStore();
  const productsStore = useProductsStore();

  const count = computed(() => stockAlertsStore.lowStocks.length);

  // The name of the product in the language of the shop, or its id while the catalog is not loaded
  function nameOf(productId: string) {
    return productsStore.products.find((product) => product.id === productId)?.name ?? productId;
  }

  async function refresh() {
    await stockAlertsStore.fetchLowStocks();
    if (count.value > 0 && productsStore.products.length === 0) await productsStore.fetchProducts();
  }

  onMounted(refresh);
  watch(() => route.fullPath, refresh);
</script>

<template>
  <v-menu v-if="count > 0" location="bottom end">
    <template #activator="{ props }">
      <v-btn v-bind="props" icon variant="text" :aria-label="t('stockAlerts.title')">
        <v-badge :content="count" color="error">
          <v-icon icon="mdi-bell-outline" />
        </v-badge>
      </v-btn>
    </template>
    <v-list density="comfortable" min-width="280" max-width="360">
      <v-list-subheader>{{ t('stockAlerts.title') }}</v-list-subheader>
      <v-list-item
        v-for="lowStock in stockAlertsStore.lowStocks"
        :key="lowStock.productId"
        :to="`/admin/products/${lowStock.productId}/edit`"
        prepend-icon="mdi-package-variant-remove"
        :title="nameOf(lowStock.productId)"
        :subtitle="t('stockAlerts.unitsLeft', { quantity: lowStock.quantity, threshold: lowStock.threshold })"
      />
    </v-list>
  </v-menu>
</template>
