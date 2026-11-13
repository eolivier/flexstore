<script setup lang="ts">
  import LoadError from '@/components/layout/LoadError.vue';
  import AdminBreadcrumbs from '@/components/admin/AdminBreadcrumbs.vue';
  import { computed, onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useEnvironmentStore } from '@/stores/environment.store';
  import { groupedByCategory, requiredButUndefined } from '@/utils/environment';
  import EnvironmentCategoryCard from './EnvironmentCategoryCard.vue';

  // The environment variables of the back end: which are defined, the secrets only as defined or not
  const { t } = useI18n();
  const environmentStore = useEnvironmentStore();
  const missingCount = computed(() => requiredButUndefined(environmentStore.variables).length);

  const groups = computed(() => groupedByCategory(environmentStore.variables));

  onMounted(() => environmentStore.fetchVariables());
</script>

<template>
  <v-container class="py-10">
    <AdminBreadcrumbs />
    <h1 class="text-h4 font-weight-bold mb-1">{{ t('environment.title') }}</h1>
    <p class="text-body-2 text-medium-emphasis mb-8">{{ t('environment.subtitle') }}</p>

    <LoadError v-if="environmentStore.loadError" :message="t('environment.loadError')" />
    <v-skeleton-loader v-else-if="!environmentStore.loaded" type="table" class="rounded-xl" />
    <template v-else>
      <v-alert v-if="missingCount > 0" type="error" variant="tonal" class="mb-4">
        {{ t('environment.missing', { n: missingCount }) }}
      </v-alert>
      <v-alert v-else type="success" variant="tonal" class="mb-4">{{ t('environment.complete') }}</v-alert>
      <EnvironmentCategoryCard
        v-for="group in groups"
        :key="group.category"
        :category="group.category"
        :variables="group.variables"
      />
      <p class="text-caption text-medium-emphasis mt-4">{{ t('environment.databaseNote') }}</p>
    </template>
  </v-container>
</template>
