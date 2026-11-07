<template>
  <!-- As tall as the icon avatar of the setting, so that the title is centered on the icon -->
  <h2 class="setting-title d-flex align-center text-subtitle-1 font-weight-bold"><slot /></h2>
</template>

<style scoped>
  .setting-title {
    min-height: 36px;
  }
</style>
