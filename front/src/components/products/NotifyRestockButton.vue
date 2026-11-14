<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useAuthStore } from '@/stores/auth.store';
  import { useRestockSubscriptionsStore } from '@/stores/restockSubscriptions.store';

  // The button of a product out of stock: the signed-in customer asks to be notified by email when it is back;
  // hidden for a visitor, and once asked it says that the customer will be notified
  const props = defineProps<{ productId: string | undefined }>();

  const { t } = useI18n();
  const authStore = useAuthStore();
  const restockSubscriptionsStore = useRestockSubscriptionsStore();

  const subscribing = ref(false);
  const failed = ref(false);
  const subscribed = computed(() => restockSubscriptionsStore.isSubscribed(props.productId));

  async function subscribe() {
    if (!props.productId) return;
    subscribing.value = true;
    failed.value = !(await restockSubscriptionsStore.subscribe(props.productId));
    subscribing.value = false;
  }
</script>

<template>
  <div v-if="authStore.isAuthenticated && productId">
    <v-btn
      v-if="subscribed"
      color="success"
      variant="tonal"
      prepend-icon="mdi-bell-check-outline"
      block
      disabled
    >
      {{ t('stock.notifyRestockDone') }}
    </v-btn>
    <v-btn
      v-else
      color="secondary"
      variant="outlined"
      prepend-icon="mdi-bell-ring-outline"
      block
      :loading="subscribing"
      @click="subscribe"
    >
      {{ t('stock.notifyRestock') }}
    </v-btn>
    <div v-if="failed" class="text-caption text-error mt-1" role="alert">{{ t('stock.notifyRestockError') }}</div>
  </div>
</template>
