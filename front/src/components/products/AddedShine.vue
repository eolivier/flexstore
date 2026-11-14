<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useAddedToCartSignal } from '@/composables/useAddedToCartSignal';

  // A diagonal liquid glass reflection (Apple style) crossing the card its parent holds when the product is added
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
  /* Liquid glass: a translucent pane that blurs and saturates what it crosses, with bright specular edges */
  .added-shine {
    position: absolute;
    top: -5%;
    bottom: -5%;
    left: 0;
    width: 34%;
    z-index: 1;
    pointer-events: none;
    border-radius: 28px;
    background: linear-gradient(
      105deg,
      rgba(255, 255, 255, 0.08),
      rgba(255, 255, 255, 0.34) 45%,
      rgba(255, 255, 255, 0.1)
    );
    backdrop-filter: blur(3px) saturate(1.8) brightness(1.12);
    -webkit-backdrop-filter: blur(3px) saturate(1.8) brightness(1.12);
    box-shadow:
      inset 1.5px 1.5px 1px rgba(255, 255, 255, 0.85),
      inset -1px -1px 1px rgba(255, 255, 255, 0.4),
      inset 0 0 18px rgba(255, 255, 255, 0.25),
      0 8px 24px rgba(31, 42, 68, 0.12);
    transform: translateX(-160%) skewX(-18deg);
    opacity: 0;
    animation: added-shine 1.1s cubic-bezier(0.3, 0.7, 0.3, 1) forwards;
  }
  @keyframes added-shine {
    15%,
    80% {
      opacity: 1;
    }
    to {
      transform: translateX(330%) skewX(-18deg);
      opacity: 0;
    }
  }
  @media (prefers-reduced-motion: reduce) {
    .added-shine {
      display: none;
    }
  }
</style>
