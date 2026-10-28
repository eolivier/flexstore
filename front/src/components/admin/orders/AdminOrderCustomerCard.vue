<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { JsonOrderCustomer } from '@/generated';

  // The customer who placed the order, to contact them; their account may not be readable any more
  const props = defineProps<{ customer: JsonOrderCustomer }>();

  const { t } = useI18n();

  const fullName = computed(() => `${props.customer.firstName} ${props.customer.lastName}`.trim());
</script>

<template>
  <v-card class="pa-5 mb-6">
    <h2 class="text-subtitle-1 font-weight-bold mb-2">{{ t('adminOrders.customer') }}</h2>
    <template v-if="customer.email">
      <p class="font-weight-medium">{{ fullName }}</p>
      <a :href="`mailto:${customer.email}`" class="text-body-2">{{ customer.email }}</a>
    </template>
    <p v-else class="text-body-2 text-medium-emphasis">{{ t('adminOrders.unknownCustomer') }}</p>
  </v-card>
</template>
