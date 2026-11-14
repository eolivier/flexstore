<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';

  const props = defineProps<{ count: number }>();
  const { t } = useI18n();

  // The cart magnifies for a moment when a product is added to it
  const magnified = ref(false);

  watch(
    () => props.count,
    (count, previousCount) => {
      if (count > previousCount) {
        magnified.value = false;
        // Next frame: the class is removed then set again so that the animation restarts at each add
        requestAnimationFrame(() => {
          magnified.value = true;
        });
      }
    }
  );
</script>

<template>
  <v-btn to="/cart" icon variant="text" :aria-label="t('nav.cart')" data-guided-tour="cart">
    <v-badge
      :content="count"
      :model-value="count > 0"
      color="secondary"
      class="cart-badge"
      :class="{ magnified }"
      @animationend="magnified = false"
    >
      <v-icon icon="mdi-cart-outline" />
    </v-badge>
  </v-btn>
</template>

<style scoped>
  .magnified {
    animation: cart-magnify 0.45s ease-in-out;
  }
  @keyframes cart-magnify {
    0%,
    100% {
      transform: scale(1);
    }
    40% {
      transform: scale(1.35);
    }
  }
  @media (prefers-reduced-motion: reduce) {
    .magnified {
      animation: none;
    }
  }
</style>
