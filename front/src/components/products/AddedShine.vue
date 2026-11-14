<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useAddedToCartSignal } from '@/composables/useAddedToCartSignal';

  // A diagonal reflection, like a light on a window, crossing the card its parent holds when the product is added
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
  .added-shine {
    position: absolute;
    top: 0;
    bottom: 0;
    left: 0;
    width: 40%;
    z-index: 1;
    pointer-events: none;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.6), transparent);
    transform: translateX(-150%) skewX(-20deg);
    animation: added-shine 0.9s ease-out forwards;
  }
  @keyframes added-shine {
    to {
      transform: translateX(350%) skewX(-20deg);
    }
  }
  @media (prefers-reduced-motion: reduce) {
    .added-shine {
      display: none;
    }
  }
</style>
