<script setup lang="ts">
  import { confettiColors } from '@/utils/guidedTour';

  // Multicolored confetti falling over the page to celebrate the end of the guided tour
  const pieceCount = 80;
  const pieces = Array.from({ length: pieceCount }, (_, index) => ({
    color: confettiColors[index % confettiColors.length],
    left: Math.random() * 100,
    delay: Math.random() * 0.8,
    duration: 2 + Math.random() * 1.5,
    drift: (Math.random() - 0.5) * 200,
    turns: 1 + Math.random() * 3,
  }));

  const styleOf = (piece: (typeof pieces)[number]) => ({
    left: `${piece.left}%`,
    background: piece.color,
    animationDelay: `${piece.delay}s`,
    animationDuration: `${piece.duration}s`,
    '--drift': `${piece.drift}px`,
    '--turns': `${piece.turns}turn`,
  });
</script>

<template>
  <div class="confetti" aria-hidden="true" data-testid="guided-tour-confetti">
    <span v-for="(piece, index) in pieces" :key="index" class="piece" :style="styleOf(piece)" />
  </div>
</template>

<style scoped>
  .confetti {
    position: fixed;
    inset: 0;
    overflow: hidden;
    pointer-events: none;
    z-index: 2500;
  }
  .piece {
    position: absolute;
    top: -20px;
    width: 8px;
    height: 14px;
    opacity: 0;
    animation-name: fall;
    animation-timing-function: ease-in;
    animation-fill-mode: forwards;
  }
  @keyframes fall {
    0% {
      opacity: 1;
      transform: translate(0, 0) rotate(0);
    }
    100% {
      opacity: 1;
      transform: translate(var(--drift), 105vh) rotate(var(--turns));
    }
  }
  @media (prefers-reduced-motion: reduce) {
    .confetti {
      display: none;
    }
  }
</style>
