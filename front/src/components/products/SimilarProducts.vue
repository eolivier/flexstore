<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { Product } from '@/models/product';
  import ProductCard from './ProductCard.vue';
  import type { SimilarProduct } from './similarProduct';

  // The other products of the category, each with its stock and rating
  defineProps<{ similarProducts: SimilarProduct[]; addingProductId?: string }>();
  defineEmits<{ (e: 'add', product: Product): void }>();

  const { t } = useI18n();
</script>

<template>
  <section v-if="similarProducts.length">
    <h2 class="text-h5 font-weight-bold mb-6">{{ t('products.similar') }}</h2>
    <v-row>
      <v-col v-for="similar in similarProducts" :key="similar.product.id" cols="12" sm="6" lg="3">
        <ProductCard
          :product="similar.product"
          :stock="similar.stock"
          :rating="similar.rating"
          :adding="addingProductId === similar.product.id"
          @add="$emit('add', $event)"
        />
      </v-col>
    </v-row>
  </section>
</template>
