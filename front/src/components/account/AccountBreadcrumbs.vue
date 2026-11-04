<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRoute } from 'vue-router';
  import { accountBreadcrumbsOf } from './accountBreadcrumbs';

  // The trail from the home of the customer's own space to the current page, to go back up in one click
  const { t } = useI18n();
  const route = useRoute();

  const items = computed(() =>
    accountBreadcrumbsOf(route.path).map((breadcrumb) => ({
      title: t(breadcrumb.label),
      to: breadcrumb.to,
      disabled: false,
    }))
  );
</script>

<template>
  <v-breadcrumbs :items="items" density="compact" class="account-breadcrumbs px-0 pt-0 mb-4">
    <template #prepend>
      <v-icon icon="mdi-account-outline" size="small" color="secondary" class="mr-2" />
    </template>
    <!-- The pages above are links; the current page is in bold -->
    <template #title="{ item }">
      <span :class="item.to ? 'breadcrumb-link' : 'font-weight-bold'">{{ item.title }}</span>
    </template>
    <template #divider>
      <v-icon icon="mdi-chevron-right" size="small" />
    </template>
  </v-breadcrumbs>
</template>

<style scoped>
  .account-breadcrumbs {
    font-size: 0.875rem;
  }
  /* The current page reads as the others, not dimmed */
  .account-breadcrumbs :deep(.v-breadcrumbs-item) {
    opacity: 1;
  }
  .breadcrumb-link {
    color: rgb(var(--v-theme-primary));
    text-decoration: underline;
    text-underline-offset: 3px;
  }
</style>
