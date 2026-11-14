<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  // Moves the whole wishlist to the cart in one click
  defineProps<{ adding: boolean; disabled: boolean }>();
  defineEmits<{ (event: 'add-all'): void }>();

  const { t } = useI18n();
</script>

<template>
  <v-btn
    color="primary"
    prepend-icon="mdi-cart-plus"
    data-testid="wishlist-add-all"
    :loading="adding"
    :disabled="disabled"
    @click="$emit('add-all')"
  >
    {{ t('wishlist.addAllToCart') }}
  </v-btn>
</template>
