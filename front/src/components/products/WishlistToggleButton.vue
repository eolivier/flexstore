<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { Product } from '@/models/product';
  import { useWishlistStore } from '@/stores/wishlist.store';
  import { useAuthStore } from '@/stores/auth.store';

  // A heart that saves or removes a product from the wishlist of the signed-in customer; hidden for a visitor
  const props = defineProps<{ product: Product }>();

  const { t } = useI18n();
  const authStore = useAuthStore();
  const wishlistStore = useWishlistStore();

  const toggling = ref(false);
  const saved = computed(() => wishlistStore.isSaved(props.product.id));
  const label = computed(() => t(saved.value ? 'products.removeFromWishlist' : 'products.addToWishlist'));

  async function toggle() {
    toggling.value = true;
    await wishlistStore.toggle(props.product);
    toggling.value = false;
  }
</script>

<template>
  <v-btn
    v-if="authStore.isAuthenticated"
    class="wishlist-toggle"
    :icon="saved ? 'mdi-heart' : 'mdi-heart-outline'"
    :color="saved ? 'error' : undefined"
    :loading="toggling"
    :aria-label="label"
    :title="label"
    variant="elevated"
    density="comfortable"
    size="small"
    @click="toggle"
  />
</template>

<style scoped>
  .wishlist-toggle {
    opacity: 0.92;
  }
</style>
