<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { PostalAddress } from '@/models/account';
  import { formatAddress } from '@/utils/address';
  import PostalAddressFields from '@/components/account/PostalAddressFields.vue';

  // One of the addresses of the customer, or another address entered here
  const address = defineModel<PostalAddress>({ required: true });
  const props = defineProps<{ addresses: PostalAddress[]; legend: string }>();

  const { t } = useI18n();

  const choice = ref<number | 'other'>(props.addresses.length > 0 ? 0 : 'other');
  const otherAddress = ref<PostalAddress>({ street: '', complement: '', postalCode: '', city: '', country: 'France' });

  watch(
    [choice, otherAddress],
    () => {
      address.value = choice.value === 'other' ? otherAddress.value : props.addresses[choice.value];
    },
    { deep: true, immediate: true }
  );
</script>

<template>
  <fieldset class="address-choice mb-4">
    <legend class="text-subtitle-1 font-weight-bold mb-2">{{ legend }}</legend>
    <v-radio-group v-model="choice" hide-details class="mb-2">
      <v-radio v-for="(saved, index) in addresses" :key="index" :value="index" :label="formatAddress(saved)" />
      <v-radio value="other" :label="t('checkout.otherAddress')" />
    </v-radio-group>
    <PostalAddressFields v-if="choice === 'other'" v-model="otherAddress" />
  </fieldset>
</template>

<style scoped>
  .address-choice {
    border: none;
  }
</style>
