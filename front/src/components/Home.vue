<script setup lang="ts">
  import { computed, onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useProductsStore } from '@/stores/products.store';
  import type { Product } from '@/models/product';
  import { useAddToCart } from '@/composables/useAddToCart';
  import ProductCard from '@/components/products/ProductCard.vue';
  import CartSnackbar from '@/components/cart/CartSnackbar.vue';
  import { categoryInfo, shopCategories } from '@/components/products/categories';

  const { t } = useI18n();
  const productsStore = useProductsStore();
  const { addingProductId, feedback, addToCart } = useAddToCart();

  // Up to 4 products, taken in turn from each category so that the selection is varied
  const featuredProducts = computed(() => {
    const byCategory = new Map<string, Product[]>();
    for (const product of productsStore.products) {
      byCategory.set(product.category, [...(byCategory.get(product.category) ?? []), product]);
    }
    const queues = [...byCategory.values()];
    const featured: Product[] = [];
    while (featured.length < 4 && queues.some(queue => queue.length)) {
      for (const queue of queues) {
        const product = queue.shift();
        if (product && featured.length < 4) featured.push(product);
      }
    }
    return featured;
  });

  onMounted(() => {
    productsStore.fetchProducts();
  });

  const values = [
    { key: 'quality', icon: 'mdi-star-outline' },
    { key: 'transparency', icon: 'mdi-tag-outline' },
    { key: 'care', icon: 'mdi-leaf' },
  ];

  const commitments = [
    { key: 'delivery', icon: 'mdi-truck-fast-outline' },
    { key: 'payment', icon: 'mdi-shield-lock-outline' },
    { key: 'returns', icon: 'mdi-swap-horizontal' },
    { key: 'support', icon: 'mdi-chat-outline' },
  ];
</script>

<template>
  <v-container class="py-12">
    <section class="intro mb-14">
      <div class="text-overline text-secondary font-weight-bold mb-2">{{ t('home.eyebrow') }}</div>
      <h1 class="text-h4 text-md-h3 font-weight-bold mb-5">{{ t('home.title') }}</h1>
      <p class="text-body-1 text-medium-emphasis mb-8">{{ t('home.intro') }}</p>
      <v-btn to="/products/" color="secondary" size="large" append-icon="mdi-arrow-right">
        {{ t('home.heroButton') }}
      </v-btn>
    </section>

    <section class="mb-14">
      <h2 class="text-h5 font-weight-bold mb-6">{{ t('home.categoriesTitle') }}</h2>
      <v-row>
        <v-col v-for="category in shopCategories" :key="category.code" cols="12" sm="4">
          <v-card
            :to="category.to"
            class="category-card pa-6 d-flex align-center"
            :style="{ background: categoryInfo(category.code).tint }"
          >
            <v-icon :icon="categoryInfo(category.code).icon" size="40" color="primary" class="mr-4" />
            <div>
              <h3 class="text-h6 font-weight-bold">{{ t(categoryInfo(category.code).label) }}</h3>
              <span class="text-body-2 text-secondary font-weight-medium">
                {{ t('home.categoryLink') }} <v-icon icon="mdi-arrow-right" size="16" />
              </span>
            </div>
          </v-card>
        </v-col>
      </v-row>
    </section>

    <section v-if="featuredProducts.length" class="mb-14">
      <div class="d-flex align-center mb-6">
        <h2 class="text-h5 font-weight-bold">{{ t('home.featuredTitle') }}</h2>
        <v-spacer />
        <v-btn to="/products/" variant="text" append-icon="mdi-arrow-right">{{ t('home.seeAll') }}</v-btn>
      </div>
      <v-row>
        <v-col v-for="product in featuredProducts" :key="product.id" cols="12" sm="6" lg="3">
          <ProductCard :product="product" :adding="addingProductId === product.id" @add="addToCart" />
        </v-col>
      </v-row>
    </section>

    <section class="mb-14">
      <h2 class="text-h5 font-weight-bold mb-6">{{ t('home.valuesTitle') }}</h2>
      <v-row>
        <v-col v-for="value in values" :key="value.key" cols="12" md="4">
          <v-card class="pa-6 h-100">
            <v-avatar color="secondary" variant="tonal" rounded="lg" size="48" class="mb-4">
              <v-icon :icon="value.icon" />
            </v-avatar>
            <h3 class="text-h6 font-weight-bold mb-2">{{ t(`home.values.${value.key}.title`) }}</h3>
            <p class="text-body-2 text-medium-emphasis">{{ t(`home.values.${value.key}.text`) }}</p>
          </v-card>
        </v-col>
      </v-row>
    </section>

    <section class="mb-14">
      <h2 class="text-h5 font-weight-bold mb-6">{{ t('home.commitmentsTitle') }}</h2>
      <v-row>
        <v-col v-for="commitment in commitments" :key="commitment.key" cols="12" sm="6">
          <div class="d-flex">
            <v-icon :icon="commitment.icon" color="primary" size="28" class="mr-4 mt-1" />
            <div>
              <h3 class="text-subtitle-1 font-weight-bold">{{ t(`home.commitments.${commitment.key}.title`) }}</h3>
              <p class="text-body-2 text-medium-emphasis">{{ t(`home.commitments.${commitment.key}.text`) }}</p>
            </div>
          </div>
        </v-col>
      </v-row>
    </section>

    <v-sheet color="primary" rounded="xl" class="cta pa-8 pa-md-10 d-flex flex-column flex-md-row align-md-center">
      <div class="mb-6 mb-md-0">
        <h2 class="text-h5 font-weight-bold mb-1">{{ t('home.ctaTitle') }}</h2>
        <p class="text-body-1 cta-text">{{ t('home.ctaText') }}</p>
      </div>
      <v-spacer />
      <v-btn to="/products/" color="secondary" size="large" append-icon="mdi-arrow-right">
        {{ t('home.ctaButton') }}
      </v-btn>
    </v-sheet>
    <CartSnackbar v-model="feedback" />
  </v-container>
</template>

<style scoped>
  .category-card {
    border: none !important;
    transition: transform 0.15s ease;
  }
  .category-card:hover {
    transform: translateY(-2px);
  }
  .intro {
    max-width: 760px;
  }
  .cta-text {
    opacity: 0.78;
  }
</style>
