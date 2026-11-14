<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRestockSubscriptionsStore } from '@/stores/restockSubscriptions.store';

  // The bell of a product the customer waits for: a click ends the subscription, so they are no longer notified
  const props = defineProps<{ productId: string }>();

  const { t } = useI18n();
  const restockSubscriptionsStore = useRestockSubscriptionsStore();

  const unsubscribing = ref(false);

  async function unsubscribe() {
    unsubscribing.value = true;
    await restockSubscriptionsStore.unsubscribe(props.productId);
    unsubscribing.value = false;
  }
</script>

<template>
  <v-btn
    icon="mdi-bell-ring"
    color="success"
    variant="elevated"
    density="comfortable"
    :loading="unsubscribing"
    :aria-label="t('stock.cancelNotifyRestock')"
    :title="t('stock.cancelNotifyRestock')"
    @click="unsubscribe"
  />
</template>
