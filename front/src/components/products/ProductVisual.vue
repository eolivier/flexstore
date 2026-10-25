<script setup lang="ts">
  import { computed } from 'vue';
  import { categoryInfo } from './categories';
  import { useProductImagesStore, type ImageFormat } from '@/stores/productImages.store';

  // The image of a product, or the icon of its category when it has none
  const props = defineProps<{
    productId: string | undefined;
    category: string | undefined;
    format: ImageFormat;
    iconSize: number;
  }>();

  const productImagesStore = useProductImagesStore();

  const category = computed(() => categoryInfo(props.category));
  const imageUrl = computed(() =>
    props.productId && productImagesStore.hasImage(props.productId)
      ? productImagesStore.imageUrl(props.productId, props.format)
      : null
  );
</script>

<template>
  <img v-if="imageUrl" :src="imageUrl" alt="" class="product-image" loading="lazy" />
  <v-icon v-else :icon="category.icon" :size="iconSize" color="primary" class="category-icon" />
</template>

<style scoped>
  .product-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: inherit;
  }
  .category-icon {
    opacity: 0.85;
  }
</style>
