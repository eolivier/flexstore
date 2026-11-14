<script setup lang="ts">
  import { computed, ref, watch } from 'vue';
  import { useProductsStore } from '@/stores/products.store';
  import { useCartStore } from '@/stores/cart.store';
  import { useStocksStore } from '@/stores/stocks.store';
  import { useProductImagesStore } from '@/stores/productImages.store';
  import { useReviewsStore } from '@/stores/reviews.store';
  import { useWishlistStore } from '@/stores/wishlist.store';
  import { useAuthStore } from '@/stores/auth.store';
  import { useAddToCart } from '@/composables/useAddToCart';
  import { useLoadedProduct } from '@/composables/useLoadedProduct';
  import { useSimilarProducts } from '@/composables/useSimilarProducts';
  import ProductReviews from '@/components/reviews/ProductReviews.vue';
  import { categoryInfo, shopCategories } from './categories';
  import ProductNotFound from './ProductNotFound.vue';
  import ProductBreadcrumbs from './ProductBreadcrumbs.vue';
  import ProductDetailVisual from './ProductDetailVisual.vue';
  import ProductSummary from './ProductSummary.vue';
  import ProductPurchase from './ProductPurchase.vue';
  import ProductCommitments from './ProductCommitments.vue';
  import SimilarProducts from './SimilarProducts.vue';

  const props = defineProps<{ id: string }>();

  const productsStore = useProductsStore();
  const cartStore = useCartStore();
  const stocksStore = useStocksStore();
  const reviewsStore = useReviewsStore();
  stocksStore.fetchStocks();
  useProductImagesStore().fetchProductsWithImage();
  if (useAuthStore().isAuthenticated) useWishlistStore().fetchWishlist();
  if (productsStore.products.length === 0) productsStore.fetchProducts();
  const { addingProductId, addToCart } = useAddToCart();
  const { product, loading, failed, loadProduct } = useLoadedProduct(() => props.id);

  const quantity = ref(1);
  watch(product, () => (quantity.value = 1));

  const category = computed(() => categoryInfo(product.value?.category));
  const categoryLink = computed(
    () => shopCategories.find((c) => c.code === product.value?.category)?.to ?? '/products'
  );
  const stock = computed(() => (product.value ? stocksStore.quantityOf(product.value.id) : null));
  const quantityInCart = computed(
    () => cartStore.cartItems?.items.find((item) => item.productId === product.value?.id)?.productQuantity ?? 0
  );
  const similarProducts = useSimilarProducts(product);

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

    <ProductNotFound v-else-if="!product" :failed="failed" @retry="loadProduct" />

    <template v-else>
      <ProductBreadcrumbs
        :product-name="product.name ?? ''"
        :category-label="category.label"
        :category-link="categoryLink"
      />

      <v-row class="mb-12">
        <v-col cols="12" md="6"><ProductDetailVisual :product="product" :tint="category.tint" /></v-col>
        <v-col cols="12" md="6" class="d-flex flex-column">
          <ProductSummary
            :product="product"
            :category-label="category.label"
            :category-link="categoryLink"
            :stock="stock"
            :rating="reviewsStore.ratingOf(product.id)"
          />
          <ProductPurchase
            v-model="quantity"
            :stock="stock"
            :adding="addingProductId === product.id"
            :quantity-in-cart="quantityInCart"
            @add="add"
          />
          <ProductCommitments />
        </v-col>
      </v-row>

      <ProductReviews v-if="product.id" :product-id="product.id" />
      <SimilarProducts :similar-products="similarProducts" :adding-product-id="addingProductId" @add="addToCart" />
    </template>
  </v-container>
</template>
