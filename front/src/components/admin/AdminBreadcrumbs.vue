<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRoute } from 'vue-router';
  import { adminBreadcrumbsOf } from './adminBreadcrumbs';

  // The trail from the home of the administration to the current page, to go back up in one click
  const { t } = useI18n();
  const route = useRoute();

  const items = computed(() =>
    adminBreadcrumbsOf(route.path).map((breadcrumb) => ({
      title: t(breadcrumb.label),
      to: breadcrumb.to,
      disabled: !breadcrumb.to,
    }))
  );
</script>

<template>
  <v-breadcrumbs :items="items" density="compact" class="admin-breadcrumbs px-0 pt-0 mb-4">
    <template #divider>
      <v-icon icon="mdi-chevron-right" size="small" />
    </template>
  </v-breadcrumbs>
</template>

<style scoped>
  .admin-breadcrumbs {
    font-size: 0.875rem;
  }
</style>
