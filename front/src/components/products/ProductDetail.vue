<script setup lang="ts">
  import { computed, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { Product } from '@/models/product';
  import { getProduct } from '@/services/product.service';
  import { useProductsStore } from '@/stores/products.store';
  import { useCartStore } from '@/stores/cart.store';
  import { useAddToCart } from '@/composables/useAddToCart';
  import ProductCard from './ProductCard.vue';
  import QuantityStepper from '@/components/cart/QuantityStepper.vue';
  import CartSnackbar from '@/components/cart/CartSnackbar.vue';
  import { categoryInfo, shopCategories } from './categories';
  import { formatPrice } from '@/utils/price';
  import { useStocksStore } from '@/stores/stocks.store';
  import StockBadge from './StockBadge.vue';
  import ProductVisual from './ProductVisual.vue';
  import { useProductImagesStore } from '@/stores/productImages.store';

  const props = defineProps<{ id: string }>();

  const { t, locale } = useI18n();
  const productsStore = useProductsStore();
  const cartStore = useCartStore();
  const stocksStore = useStocksStore();
  stocksStore.fetchStocks();
  useProductImagesStore().fetchProductsWithImage();
  const { addingProductId, feedback, addToCart } = useAddToCart();

  const product = ref<Product | null>(null);
  const loading = ref(true);
  const failed = ref(false);
  const quantity = ref(1);

  async function loadProduct() {
    loading.value = true;
    failed.value = false;
    quantity.value = 1;
    try {
      product.value = await getProduct(props.id, locale.value);
    } catch {
      product.value = null;
      failed.value = true;
    } finally {
      loading.value = false;
    }
  }

  // Reload when navigating from one product to another (e.g. a similar product), or when the customer changes
  // the language: the product is loaded in it only
  watch([() => props.id, locale], loadProduct, { immediate: true });

  if (productsStore.products.length === 0) {
    productsStore.fetchProducts();
  }

  const category = computed(() => categoryInfo(product.value?.category));
  const categoryLink = computed(
    () => shopCategories.find((c) => c.code === product.value?.category)?.to ?? '/products/'
  );
  const stock = computed(() => (product.value ? stocksStore.quantityOf(product.value.id) : null));
  const price = computed(() => formatPrice(product.value?.price, product.value?.currency, locale.value));
  const quantityInCart = computed(
    () => cartStore.cartItems?.items.find((item) => item.productId === product.value?.id)?.productQuantity ?? 0
  );
  // The other products of the category that can be ordered: the products out of stock are left out
  const similarProducts = computed(() =>
    productsStore.products
      .filter((p) => p.category === product.value?.category && p.id !== product.value?.id)
      .filter((p) => stocksStore.quantityOf(p.id) !== 0)
      .slice(0, 4)
  );

  async function add() {
    if (!product.value) return;
    await addToCart(product.value, quantity.value);
    quantity.value = 1;
  }
</script>

<template>
  <v-container class="py-10">
    <v-row v-if="loading">
      <v-col cols="12" md="6"><v-skeleton-loader type="image" height="380" class="rounded-xl" /></v-col>
      <v-col cols="12" md="6"><v-skeleton-loader type="article, actions" class="rounded-xl" /></v-col>
    </v-row>

    <v-card v-else-if="!product" class="pa-10 text-center">
      <v-icon
        :icon="failed ? 'mdi-alert-circle-outline' : 'mdi-package-variant-remove'"
        size="56"
        class="text-medium-emphasis mb-3"
      />
      <h1 class="text-h6 font-weight-bold mb-1">{{ failed ? t('products.error') : t('products.notFound') }}</h1>
      <p v-if="!failed" class="text-body-2 text-medium-emphasis mb-6">{{ t('products.notFoundText') }}</p>
      <div class="d-flex justify-center ga-2">
        <v-btn v-if="failed" variant="outlined" color="primary" @click="loadProduct">{{
          t('products.retry')
        }}</v-btn>
        <v-btn to="/products/" color="secondary" prepend-icon="mdi-arrow-left">{{
          t('products.backToShop')
        }}</v-btn>
      </div>
    </v-card>

    <template v-else>
      <v-breadcrumbs
        :items="[
          { title: t('products.shop'), to: '/products/' },
          { title: t(category.label), to: categoryLink },
          { title: product.name ?? '', disabled: true },
        ]"
        class="px-0 pt-0 mb-4"
      >
        <template #divider><v-icon icon="mdi-chevron-right" size="16" /></template>
      </v-breadcrumbs>

      <v-row class="mb-12">
        <v-col cols="12" md="6">
          <div class="detail-visual d-flex align-center justify-center" :style="{ background: category.tint }">
            <ProductVisual
              :product-id="product.id"
              :category="product.category"
              format="large"
              :icon-size="140"
            />
          </div>
        </v-col>

        <v-col cols="12" md="6" class="d-flex flex-column">
          <router-link
            :to="categoryLink"
            class="category-link text-caption text-secondary font-weight-bold text-uppercase mb-2"
          >
            {{ t(category.label) }}
          </router-link>
          <h1 class="text-h4 font-weight-bold mb-3">{{ product.name }}</h1>
          <div class="d-flex align-center flex-wrap ga-3 mb-5">
            <span class="text-h4 font-weight-bold text-primary">{{ price }}</span>
            <StockBadge v-if="stock !== null" :quantity="stock" />
          </div>
          <p class="text-body-1 text-medium-emphasis mb-8">{{ product.description }}</p>

          <div class="d-flex flex-wrap align-center ga-4 mb-3">
            <div>
              <div class="text-caption text-medium-emphasis mb-1">{{ t('products.quantity') }}</div>
              <QuantityStepper v-model="quantity" :min="1" />
            </div>
            <v-btn
              color="primary"
              size="large"
              prepend-icon="mdi-cart-plus"
              class="flex-grow-1 align-self-end"
              :loading="addingProductId === product.id"
              :disabled="stock === 0"
              @click="add"
            >
              {{ t('products.addToCart') }}
            </v-btn>
          </div>

          <div class="text-body-2 text-medium-emphasis mb-8">
            <v-icon icon="mdi-cart-outline" size="16" class="mr-1" />
            {{ t('products.inCart', quantityInCart) }}
            <router-link v-if="quantityInCart > 0" to="/cart" class="text-secondary ml-1">{{
              t('products.viewCart')
            }}</router-link>
          </div>

          <v-divider class="mb-6" />
          <div class="d-flex flex-column ga-3">
            <div class="d-flex align-center text-body-2">
              <v-icon icon="mdi-truck-fast-outline" color="primary" class="mr-3" />
              {{ t('home.commitments.delivery.title') }}
            </div>
            <div class="d-flex align-center text-body-2">
              <v-icon icon="mdi-swap-horizontal" color="primary" class="mr-3" />
              {{ t('home.commitments.returns.title') }}
            </div>
            <div class="d-flex align-center text-body-2">
              <v-icon icon="mdi-shield-lock-outline" color="primary" class="mr-3" />
              {{ t('home.commitments.payment.title') }}
            </div>
          </div>
        </v-col>
      </v-row>

      <section v-if="similarProducts.length">
        <h2 class="text-h5 font-weight-bold mb-6">{{ t('products.similar') }}</h2>
        <v-row>
          <v-col v-for="similar in similarProducts" :key="similar.id" cols="12" sm="6" lg="3">
            <ProductCard
              :product="similar"
              :stock="stocksStore.quantityOf(similar.id)"
              :adding="addingProductId === similar.id"
              @add="addToCart"
            />
          </v-col>
        </v-row>
      </section>
    </template>

    <CartSnackbar v-model="feedback" />
  </v-container>
</template>

<style scoped>
  .detail-visual {
    aspect-ratio: 4 / 3;
    border-radius: 24px;
    overflow: hidden;
  }
  .detail-icon {
    opacity: 0.85;
  }
  .category-link {
    text-decoration: none;
    align-self: flex-start;
  }
</style>
