<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRouter } from 'vue-router';
  import { useAccountStore } from '@/stores/account.store';
  import { useCartStore } from '@/stores/cart.store';
  import { useOrdersStore } from '@/stores/orders.store';
  import type { PostalAddress } from '@/models/account';
  import AddressChoice from './AddressChoice.vue';
  import CheckoutSummary from './CheckoutSummary.vue';

  const { t } = useI18n();
  const router = useRouter();
  const accountStore = useAccountStore();
  const cartStore = useCartStore();
  const ordersStore = useOrdersStore();

  // null while the addresses of the customer are loaded
  const savedAddresses = ref<PostalAddress[] | null>(null);
  const delivery = ref<PostalAddress>({ street: '', complement: '', postalCode: '', city: '', country: 'France' });
  const billing = ref<PostalAddress>({ street: '', complement: '', postalCode: '', city: '', country: 'France' });
  // Most orders are billed to their delivery address
  const billedToDelivery = ref(true);
  const valid = ref<boolean | null>(null);
  const placing = ref(false);
  const error = ref<string | null>(null);

  onMounted(async () => {
    cartStore.fetchCartItems();
    const account = await accountStore.fetchAccount();
    savedAddresses.value = (account?.postalAddresses ?? []).map(address => ({ ...address, complement: address.complement ?? '' }));
  });

  async function placeOrder() {
    placing.value = true;
    error.value = null;
    const placement = await ordersStore.placeOrder({
      deliveryAddress: delivery.value,
      billingAddress: billedToDelivery.value ? undefined : billing.value,
    });
    placing.value = false;
    if (placement.status === 'placed') {
      router.push(`/orders/${placement.order.id}/confirmation`);
    } else {
      error.value = placement.status;
    }
  }
</script>

<template>
  <v-container class="py-10">
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('checkout.title') }}</h1>
    <v-row>
      <v-col cols="12" md="8">
        <v-skeleton-loader v-if="savedAddresses === null" type="article" class="rounded-xl" />
        <v-card v-else class="pa-6">
          <v-form v-model="valid">
            <AddressChoice v-model="delivery" :addresses="savedAddresses" :legend="t('checkout.deliveryAddress')" />
            <v-checkbox v-model="billedToDelivery" :label="t('checkout.billedToDelivery')" hide-details class="mb-2" />
            <AddressChoice v-if="!billedToDelivery" v-model="billing" :addresses="savedAddresses" :legend="t('checkout.billingAddress')" />
          </v-form>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <CheckoutSummary :placing="placing" :disabled="savedAddresses === null || valid === false" :error="error" @place="placeOrder" />
      </v-col>
    </v-row>
  </v-container>
</template>
