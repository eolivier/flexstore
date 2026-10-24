<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRouter } from 'vue-router';
  import { useAuthStore } from '@/stores/auth.store';
  import { useCartStore } from '@/stores/cart.store';
  import { useOrdersStore } from '@/stores/orders.store';
  import type { Item } from '@/models/cart';
  import CartLine from '@/components/cart/CartLine.vue';
  import { formatPrice } from '@/utils/price';

  const cartStore = useCartStore();
  const authStore = useAuthStore();
  const ordersStore = useOrdersStore();
  const router = useRouter();
  const { t, locale } = useI18n();

  const placingOrder = ref(false);
  const orderError = ref(false);

  // A visitor signs in first and comes back to the cart, whose items are kept (merged into the account cart)
  async function placeOrder() {
    if (!authStore.isAuthenticated) {
      router.push({ path: '/login', query: { redirect: '/cart' } });
      return;
    }
    placingOrder.value = true;
    orderError.value = false;
    const placement = await ordersStore.placeOrder();
    placingOrder.value = false;
    if (placement.status === 'placed') {
      router.push({ path: `/orders/${placement.order.id}`, query: { placed: 'true' } });
    } else {
      orderError.value = true;
    }
  }

  onMounted(() => {
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
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('cart.title') }}</h1>

    <v-row v-if="!cartStore.cartItems">
      <v-col cols="12" md="8"><v-skeleton-loader type="list-item-avatar-two-line@3" class="rounded-xl" /></v-col>
      <v-col cols="12" md="4"><v-skeleton-loader type="article" class="rounded-xl" /></v-col>
    </v-row>

    <v-card v-else-if="items.length === 0" class="pa-10 text-center">
      <v-icon icon="mdi-cart-outline" size="56" class="text-medium-emphasis mb-3" />
      <h2 class="text-h6 font-weight-bold mb-1">{{ t('cart.empty') }}</h2>
      <p class="text-body-2 text-medium-emphasis mb-6">{{ t('cart.emptyText') }}</p>
      <v-btn to="/products/" color="secondary" size="large" append-icon="mdi-arrow-right">
        {{ t('home.heroButton') }}
      </v-btn>
    </v-card>

    <v-row v-else>
      <v-col cols="12" md="8">
        <v-card class="px-5">
          <template v-for="(item, index) in items" :key="item.itemId">
            <v-divider v-if="index > 0" />
            <CartLine :item="item" @update:quantity="quantity => updateQuantityAndSave(item, quantity)" />
          </template>
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
          <v-alert v-if="orderError" type="error" variant="tonal" density="compact" class="mb-4">
            {{ t('cart.orderError') }}
          </v-alert>
          <v-btn
            color="secondary"
            size="large"
            block
            class="mb-3"
            prepend-icon="mdi-check"
            :loading="placingOrder"
            @click="placeOrder"
          >
            {{ t('cart.placeOrder') }}
          </v-btn>
          <p v-if="!authStore.isAuthenticated" class="text-caption text-medium-emphasis text-center mb-3">
            {{ t('cart.signInToOrder') }}
          </p>
          <v-btn to="/products/" variant="outlined" color="primary" block prepend-icon="mdi-arrow-left">
            {{ t('cart.continue') }}
          </v-btn>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped>
  .summary {
    position: sticky;
    top: 96px;
  }
</style>
