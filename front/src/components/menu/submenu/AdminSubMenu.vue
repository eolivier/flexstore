<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { adminLinks } from '../navigation';

  const { t } = useI18n();
  const adminOpen = ref(false);
</script>

<template>
  <v-list-group v-model="adminOpen" value="admin">
    <template #activator="{ props }">
      <v-list-item v-bind="props" prepend-icon="mdi-cog-outline" :title="t('nav.admin')" />
    </template>
    <v-list-item
      v-for="link in adminLinks"
      :key="link.to"
      :to="link.to"
      :prepend-icon="link.icon"
      :title="t(link.label)"
    />
  </v-list-group>
</template>
