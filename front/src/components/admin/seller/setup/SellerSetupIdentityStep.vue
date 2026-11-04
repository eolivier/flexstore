<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { useSellerFieldRules } from './useSellerFieldRules';

  const name = defineModel<string>('name', { required: true });
  const address = defineModel<string>('address', { required: true });
  const legalForm = defineModel<string>('legalForm', { required: true });
  const shareCapital = defineModel<string>('shareCapital', { required: true });

  const { t } = useI18n();
  const rules = useSellerFieldRules();
</script>

<template>
  <div>
    <p class="text-body-2 text-medium-emphasis mb-4">{{ t('sellerSetup.identityText') }}</p>
    <v-text-field v-model="name" :label="t('seller.name')" :rules="[rules.required]" class="mb-2" />
    <v-text-field
      v-model="address"
      :label="t('seller.address')"
      :hint="t('seller.addressHint')"
      :rules="[rules.required]"
      class="mb-2"
    />
    <v-text-field
      v-model="legalForm"
      :label="t('seller.legalForm')"
      :hint="t('seller.legalFormHint')"
      :rules="[rules.required]"
      class="mb-2"
    />
    <v-text-field
      v-model="shareCapital"
      :label="t('seller.shareCapital')"
      :hint="t('seller.shareCapitalHint')"
      :rules="[rules.shareCapital]"
    />
  </div>
</template>
