<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { useSellerFieldRules } from './useSellerFieldRules';

  const phone = defineModel<string>('phone', { required: true });
  const email = defineModel<string>('email', { required: true });
  const website = defineModel<string>('website', { required: true });

  const { t } = useI18n();
  const rules = useSellerFieldRules();
</script>

<template>
  <div>
    <p class="text-body-2 text-medium-emphasis mb-4">{{ t('sellerSetup.contactText') }}</p>
    <v-text-field
      v-model="phone"
      :label="t('seller.phone')"
      :rules="[rules.phone]"
      type="tel"
      prepend-inner-icon="mdi-phone-outline"
      class="mb-2"
    />
    <v-text-field
      v-model="email"
      :label="t('seller.email')"
      :rules="[rules.email]"
      type="email"
      prepend-inner-icon="mdi-email-outline"
      class="mb-2"
    />
    <v-text-field
      v-model="website"
      :label="t('seller.website')"
      :hint="t('seller.websiteHint')"
      :rules="[rules.website]"
      prepend-inner-icon="mdi-web"
    />
  </div>
</template>
