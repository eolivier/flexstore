<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  defineProps<{ links: { to: string; icon: string; label: string }[] }>();
  const { t } = useI18n();
</script>

<template>
  <v-list-item
    v-for="link in links"
    :key="link.to"
    :to="link.to"
    :prepend-icon="link.icon"
    :title="t(link.label)"
    :data-guided-tour="link.to"
  />
</template>
