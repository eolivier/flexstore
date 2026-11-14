<script setup lang="ts">
  import EmptyState from '@/components/layout/EmptyState.vue';
  import { computed, onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRouter } from 'vue-router';
  import { useAuthStore } from '@/stores/auth.store';
  import { useCartStore } from '@/stores/cart.store';
  import { useProductImagesStore } from '@/stores/productImages.store';
  import type { Item } from '@/models/cart';
  import OrderSteps from '@/components/checkout/OrderSteps.vue';
  import CartLine from '@/components/cart/CartLine.vue';
  import { formatPrice } from '@/utils/price';

  const cartStore = useCartStore();
  const authStore = useAuthStore();
  const router = useRouter();
  const { t, locale } = useI18n();

  // The order is placed on the checkout page, where the customer chooses the addresses. A visitor signs in first
  // (the checkout page needs a signed-in customer) and their cart is kept, merged into the account cart
  function goToCheckout() {
    router.push('/checkout');
  }

  onMounted(() => {
    useProductImagesStore().fetchProductsWithImage();
    cartStore.fetchCartItems();
  });

  const items = computed(() => cartStore.cartItems?.items ?? []);
  const itemsCount = computed(() => items.value.reduce((count, item) => count + item.productQuantity, 0));
  const total = computed(() =>
    formatPrice(cartStore.cartItems?.totalItemsPrice, cartStore.cartItems?.itemsCurrency, locale.value)
  );

  function updateQuantityAndSave(item: Item, quantity: number) {
    const updatedItem = { ...item, productQuantity: quantity, itemPrice: quantity * (item.productPrice ?? 0) };
    if (quantity === 0) {
      cartStore.removeItem(updatedItem);
    } else {
      cartStore.saveItem(updatedItem);
    }
  }
</script>

<template>
  <v-container class="py-10">
    <OrderSteps current="cart" />
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('cart.title') }}</h1>

    <transition name="cart-fade" mode="out-in">
      <v-row v-if="!cartStore.cartItems" key="loading">
        <v-col cols="12" md="8"
          ><v-skeleton-loader type="list-item-avatar-two-line@3" class="rounded-xl"
        /></v-col>
        <v-col cols="12" md="4"><v-skeleton-loader type="article" class="rounded-xl" /></v-col>
      </v-row>

      <EmptyState
        v-else-if="items.length === 0"
        key="empty"
        icon="mdi-cart-outline"
        :message="t('cart.empty')"
        :description="t('cart.emptyText')"
      >
        <v-btn to="/products" color="secondary" size="large" append-icon="mdi-arrow-right">
          {{ t('home.heroButton') }}
        </v-btn>
      </EmptyState>

      <v-row v-else key="lines">
        <v-col cols="12" md="8">
          <v-card class="px-5">
            <transition-group name="cart-line-fade" tag="div">
              <div v-for="item in items" :key="item.itemId" class="cart-line-row">
                <CartLine :item="item" @update:quantity="(quantity) => updateQuantityAndSave(item, quantity)" />
              </div>
            </transition-group>
          </v-card>
        </v-col>

        <v-col cols="12" md="4">
          <v-card class="summary pa-6">
            <h2 class="text-h6 font-weight-bold mb-4">{{ t('cart.summary') }}</h2>
            <div class="d-flex text-body-2 text-medium-emphasis mb-2">
              <span>{{ t('cart.itemsCount', itemsCount) }}</span>
            </div>
            <v-divider class="my-4" />
            <div class="d-flex align-center mb-6">
              <span class="text-subtitle-1 font-weight-bold">{{ t('cart.total') }}</span>
              <v-spacer />
              <span class="text-h5 font-weight-bold">{{ total }}</span>
            </div>
            <v-btn
              color="secondary"
              size="large"
              block
              class="mb-3"
              prepend-icon="mdi-check"
              @click="goToCheckout"
            >
              {{ t('cart.placeOrder') }}
            </v-btn>
            <p v-if="!authStore.isAuthenticated" class="text-caption text-medium-emphasis text-center mb-3">
              {{ t('cart.signInToOrder') }}
            </p>
            <v-btn to="/products" variant="outlined" color="primary" block prepend-icon="mdi-arrow-left">
              {{ t('cart.continue') }}
            </v-btn>
          </v-card>
        </v-col>
      </v-row>
    </transition>
  </v-container>
</template>

<style scoped>
  .summary {
    position: sticky;
    top: 96px;
  }
  .cart-line-row + .cart-line-row {
    border-top: thin solid rgba(var(--v-border-color), var(--v-border-opacity));
  }
  .cart-line-fade-leave-active,
  .cart-fade-enter-active,
  .cart-fade-leave-active {
    transition: opacity 0.5s ease;
  }
  .cart-line-fade-leave-to,
  .cart-fade-enter-from,
  .cart-fade-leave-to {
    opacity: 0;
  }
  @media (prefers-reduced-motion: reduce) {
    .cart-line-fade-leave-active,
    .cart-fade-enter-active,
    .cart-fade-leave-active {
      transition: none;
    }
  }
</style>
