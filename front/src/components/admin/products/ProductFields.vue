<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { categoryInfo, shopCategories } from '@/components/products/categories';
  import type { ProductTexts } from '@/models/product';
  import ProductTranslationFields from './ProductTranslationFields.vue';
  import SkuField from './SkuField.vue';

  // The fields of a product, shared by its creation and its edition. skuAlreadyUsed: the API refused the SKU,
  // given to another product
  defineProps<{ skuAlreadyUsed: boolean }>();
  const sku = defineModel<string>('sku', { required: true });
  const texts = defineModel<ProductTexts>('texts', { required: true });
  const category = defineModel<string>('category', { required: true });
  const price = defineModel<string | null>('price', { required: true });

  const { t } = useI18n();

  const categories = computed(() =>
    shopCategories.map(({ code }) => ({
      title: t(categoryInfo(code).label),
      value: code,
      props: { prependIcon: categoryInfo(code).icon },
    }))
  );

  const rules = {
    required: (v: string | null) => !!v || t('createProduct.mandatoryField'),
    price: (v: string | null) => (v !== null && parseFloat(v) > 0) || t('createProduct.invalidPrice'),
  };
</script>

<template>
  <SkuField v-model="sku" :already-used="skuAlreadyUsed" />
  <ProductTranslationFields v-model="texts" />
  <v-row>
    <v-col cols="12" sm="7">
      <v-select v-model="category" :items="categories" :label="t('createProduct.category')" />
    </v-col>
    <v-col cols="12" sm="5">
      <v-text-field
        v-model="price"
        :label="t('createProduct.price')"
        :rules="[rules.required, rules.price]"
        type="number"
        min="0"
        step="0.01"
        required
      />
    </v-col>
  </v-row>
</template>
