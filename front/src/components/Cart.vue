<script setup>
  import { computed, onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useCartStore } from '@/stores/cart.store.ts';

  const cartStore = useCartStore();
  const { t } = useI18n();

  onMounted(() => {
    cartStore.fetchCartItems();
  });

  const headers = computed(() => [
    { title: t('table.itemId'), key: 'itemId' },
    { title: t('table.product'), key: 'productName' },
    { title: t('table.description'), key: 'productDescription' },
    { title: t('table.quantity'), key: 'productQuantity' },
    { title: t('table.price'), key: 'itemPrice' },
  ]);

  function incrementQuantityAndSave(item) {
    item.productQuantity++;
    item.itemPrice = item.productQuantity * item.productPrice;
    cartStore.cartItems.totalItemsPrice = calculateTotal();
    cartStore.saveItem(item);
  }

  function decrementQuantityAndSave(item) {
    item.productQuantity--;
    item.itemPrice = item.productQuantity * item.productPrice;
    if (item.productQuantity === 0) {
      cartStore.removeItem(item);
    } else {
      cartStore.saveItem(item);
    }
    cartStore.cartItems.totalItemsPrice = calculateTotal();
  }

  function calculateTotal() {
    return cartStore.cartItems.items.reduce((total, item) => total + item.itemPrice, 0);
  }
</script>
<template>
  <v-container class="py-10">
    <h1 class="text-h5 font-weight-bold mb-6">{{ t('cart.title') }}</h1>

    <v-data-table
      :headers="headers"
      :items="cartStore.cartItems?.items || []"
      class="elevation-2"
      hide-default-footer
    >
      <template #item.productQuantity="{ item }">
        <v-row align="center" no-gutters>
          <v-btn icon @click="decrementQuantityAndSave(item)">
            <v-icon>mdi-minus</v-icon>
          </v-btn>
          <v-text-field
            v-model="item.productQuantity"
            type="number"
            min="1"
            class="mx-2"
            style="width: 60px"
            hide-details
            dense
          />
          <v-btn icon @click="incrementQuantityAndSave(item)">
            <v-icon>mdi-plus</v-icon>
          </v-btn>
        </v-row>
      </template>
      <template #item.itemPrice="{ item }" class="text-right">
        <strong>{{ item.itemPrice.toFixed(2) }} {{ item.productCurrency }}</strong>
      </template>
    </v-data-table>
    <v-card class="mt-4 pa-4 text-right" outlined>
      <span class="font-weight-bold">{{ t('cart.total') }}&nbsp;:&nbsp;</span>
      <strong>
        {{ cartStore.cartItems?.totalItemsPrice?.toFixed(2) ?? '0.00' }}
        {{ cartStore.cartItems?.itemsCurrency ?? '' }}
      </strong>
    </v-card>
  </v-container>
</template>
