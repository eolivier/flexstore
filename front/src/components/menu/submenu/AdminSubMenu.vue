<script setup lang="ts">
  import NavLinkItems from '../NavLinkItems.vue';
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
    <NavLinkItems :links="adminLinks" />
  </v-list-group>
</template>
