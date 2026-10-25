<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { categoryInfo, shopCategories } from '@/components/products/categories';
  import type { ProductFilter } from '@/utils/productFilter';

  // The filters of the list of products: a text, a category, a state of the stock
  const filter = defineModel<ProductFilter>({ required: true });

  const { t } = useI18n();

  const categories = computed(() =>
    shopCategories.map((category) => ({ title: t(categoryInfo(category.code).label), value: category.code }))
  );
  const stockStates = computed(() => [
    { title: t('stock.outOfStock'), value: 'outOfStock' },
    { title: t('adminProducts.fewLeft'), value: 'fewLeft' },
    { title: t('stock.inStock'), value: 'inStock' },
  ]);
</script>

<template>
  <v-row dense class="mb-2">
    <v-col cols="12" md="6">
      <v-text-field
        v-model="filter.text"
        :label="t('adminProducts.search')"
        prepend-inner-icon="mdi-magnify"
        clearable
        hide-details
        @click:clear="filter.text = ''"
      />
    </v-col>
    <v-col cols="12" sm="6" md="3">
      <v-select
        v-model="filter.category"
        :items="categories"
        :label="t('table.category')"
        clearable
        hide-details
      />
    </v-col>
    <v-col cols="12" sm="6" md="3">
      <v-select
        v-model="filter.stockState"
        :items="stockStates"
        :label="t('table.stock')"
        clearable
        hide-details
      />
    </v-col>
  </v-row>
</template>
