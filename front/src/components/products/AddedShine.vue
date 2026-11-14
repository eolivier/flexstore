<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useAddedToCartSignal } from '@/composables/useAddedToCartSignal';

  // A liquid glass veil (Apple style) covering the card its parent holds and fading out when the product is added
  const props = defineProps<{ productId?: string }>();

  const { lastAddedProduct } = useAddedToCartSignal();
  const shineCount = ref(0);

  watch(lastAddedProduct, (added) => {
    if (added?.productId === props.productId) shineCount.value++;
  });
</script>

<template>
  <span v-if="shineCount > 0" :key="shineCount" class="added-shine" data-testid="added-shine" aria-hidden="true" />
</template>

<style scoped>
  /* Liquid glass over the whole card: translucent, blurs and saturates the card, then fades out */
  .added-shine {
    position: absolute;
    inset: 0;
    z-index: 1;
    pointer-events: none;
    border-radius: inherit;
    background: linear-gradient(
      135deg,
      rgba(255, 255, 255, 0.34),
      rgba(255, 255, 255, 0.1) 50%,
      rgba(255, 255, 255, 0.24)
    );
    backdrop-filter: blur(3px) saturate(1.8) brightness(1.12);
    -webkit-backdrop-filter: blur(3px) saturate(1.8) brightness(1.12);
    box-shadow:
      inset 1.5px 1.5px 1px rgba(255, 255, 255, 0.85),
      inset -1px -1px 1px rgba(255, 255, 255, 0.4),
      inset 0 0 24px rgba(255, 255, 255, 0.3);
    animation: added-shine 1.4s ease-out forwards;
  }
  @keyframes added-shine {
    from {
      opacity: 1;
    }
    to {
      opacity: 0;
    }
  }
  @media (prefers-reduced-motion: reduce) {
    .added-shine {
      display: none;
    }
  }
</style>
