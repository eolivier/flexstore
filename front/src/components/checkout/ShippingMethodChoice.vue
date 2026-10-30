<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { JsonOrderAddressesShippingMethodEnum, JsonShippingOptions } from '@/generated';
  import { formatPrice } from '@/utils/price';

  // The shipping method the customer chooses, each one at its cost for their cart
  const shippingMethod = defineModel<JsonOrderAddressesShippingMethodEnum>({ required: true });
  defineProps<{ shippingOptions: JsonShippingOptions }>();

  const { t, locale } = useI18n();
</script>

<template>
  <div class="mb-2">
    <h2 class="text-subtitle-1 font-weight-bold mb-1">{{ t('checkout.shipping.title') }}</h2>
    <p class="text-body-2 text-medium-emphasis mb-2">
      {{
        t('checkout.shipping.freeFrom', {
          threshold: formatPrice(shippingOptions.freeShippingThreshold, shippingOptions.currency, locale),
        })
      }}
    </p>
    <v-radio-group v-model="shippingMethod" hide-details>
      <v-radio v-for="option in shippingOptions.options" :key="option.method" :value="option.method">
        <template #label>
          <div class="d-flex flex-wrap ga-2 align-baseline">
            <span class="font-weight-medium">{{ t(`checkout.shipping.methods.${option.method}`) }}</span>
            <span class="text-body-2 text-medium-emphasis">{{
              t(`checkout.shipping.delays.${option.method}`)
            }}</span>
            <span class="font-weight-bold">
              {{
                option.cost === 0
                  ? t('checkout.shipping.free')
                  : formatPrice(option.cost, shippingOptions.currency, locale)
              }}
            </span>
          </div>
        </template>
      </v-radio>
    </v-radio-group>
  </div>
</template>
