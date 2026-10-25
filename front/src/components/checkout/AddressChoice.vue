<script setup lang="ts">
  import { computed, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { PostalAddress, Recipient } from '@/models/account';
  import { formatAddress, isRecipientComplete } from '@/utils/address';
  import PostalAddressFields from '@/components/account/PostalAddressFields.vue';
  import RecipientFields from '@/components/account/RecipientFields.vue';

  // One of the addresses of the customer, or another address entered here, sent by default to the customer
  const address = defineModel<PostalAddress>({ required: true });
  const props = defineProps<{
    addresses: PostalAddress[];
    customer: Recipient;
    purpose: 'delivery' | 'billing';
  }>();

  const { t } = useI18n();

  const choice = ref<number | 'other'>(props.addresses.length > 0 ? 0 : 'other');
  const otherLocation = { street: '', complement: '', postalCode: '', city: '', country: 'France' };
  const otherAddress = ref<PostalAddress>({ ...props.customer, ...otherLocation });
  // A saved address whose recipient is incomplete is completed here before ordering: it is edited in a copy
  const savedAddresses = ref<PostalAddress[]>(props.addresses.map((saved) => ({ ...saved })));
  const incompleteChoices = props.addresses
    .map((saved, index) => (isRecipientComplete(saved) ? null : index))
    .filter((index) => index !== null);
  const recipientToComplete = computed(
    () => choice.value !== 'other' && incompleteChoices.includes(choice.value)
  );

  watch(
    [choice, otherAddress, savedAddresses],
    () => {
      address.value =
        choice.value === 'other' ? otherAddress.value : savedAddresses.value[choice.value];
    },
    { deep: true, immediate: true }
  );
</script>

<template>
  <fieldset class="address-choice mb-4">
    <legend class="text-subtitle-1 font-weight-bold mb-2">
      {{ t(`checkout.${purpose}Address`) }}
    </legend>
    <v-radio-group v-model="choice" hide-details class="mb-2">
      <v-radio
        v-for="(saved, index) in addresses"
        :key="index"
        :value="index"
        :label="formatAddress(saved)"
      />
      <v-radio value="other" :label="t('checkout.otherAddress')" />
    </v-radio-group>
    <template v-if="recipientToComplete && choice !== 'other'">
      <p class="text-body-2 text-medium-emphasis mb-2">{{ t('checkout.completeRecipient') }}</p>
      <RecipientFields v-model="savedAddresses[choice]" :purpose="purpose" />
    </template>
    <template v-if="choice === 'other'">
      <RecipientFields v-model="otherAddress" :purpose="purpose" />
      <PostalAddressFields v-model="otherAddress" />
    </template>
  </fieldset>
</template>

<style scoped>
  .address-choice {
    border: none;
  }
</style>
