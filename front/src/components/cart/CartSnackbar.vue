<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { CartFeedback } from '@/composables/useAddToCart';

  const feedback = defineModel<CartFeedback>({ required: true });

  const { t } = useI18n();
</script>

<template>
  <v-snackbar v-model="feedback.show" :color="feedback.success ? 'primary' : 'error'" timeout="3000" location="bottom right">
    <v-icon :icon="feedback.success ? 'mdi-check-circle-outline' : 'mdi-alert-circle-outline'" class="mr-2" />
    {{ feedback.text }}
    <template v-if="feedback.success" #actions>
      <v-btn to="/cart" variant="text" color="secondary">{{ t('products.viewCart') }}</v-btn>
    </template>
  </v-snackbar>
</template>
