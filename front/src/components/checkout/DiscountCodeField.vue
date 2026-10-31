<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { JsonDiscountPreview } from '@/generated';
  import { useOrdersStore } from '@/stores/orders.store';
  import { formatPrice } from '@/utils/price';

  // The discount code of the order: entered and checked against the cart, then shown with what it takes off;
  // null: no code applied
  const appliedDiscount = defineModel<JsonDiscountPreview | null>({ required: true });

  const { t, locale } = useI18n();
  const ordersStore = useOrdersStore();

  const code = ref('');
  const checking = ref(false);
  const refusal = ref<'unknown' | 'unusable' | 'error' | null>(null);

  async function apply() {
    if (!code.value.trim()) return;
    checking.value = true;
    const discountCheck = await ordersStore.checkDiscountCode(code.value.trim());
    checking.value = false;
    if (discountCheck.status === 'applied') {
      appliedDiscount.value = discountCheck.preview;
      refusal.value = null;
      code.value = '';
    } else {
      refusal.value = discountCheck.status;
    }
  }
</script>

<template>
  <div>
    <h2 class="text-subtitle-1 font-weight-bold mb-2">{{ t('checkout.discount.title') }}</h2>
    <v-chip
      v-if="appliedDiscount"
      color="success"
      variant="tonal"
      prepend-icon="mdi-ticket-percent-outline"
      closable
      :close-label="t('checkout.discount.remove')"
      @click:close="appliedDiscount = null"
    >
      {{ appliedDiscount.code }} : −{{
        formatPrice(appliedDiscount.amountOff, appliedDiscount.currency, locale)
      }}
    </v-chip>
    <div v-else class="d-flex align-start ga-2">
      <v-text-field
        v-model="code"
        :label="t('checkout.discount.code')"
        :error-messages="refusal ? t(`checkout.discount.errors.${refusal}`) : []"
        density="compact"
        class="code"
        @keydown.enter.prevent="apply"
        @update:model-value="refusal = null"
      />
      <v-btn variant="tonal" color="primary" :loading="checking" @click="apply">
        {{ t('checkout.discount.apply') }}
      </v-btn>
    </div>
  </div>
</template>

<style scoped>
  .code :deep(input) {
    text-transform: uppercase;
  }
</style>
