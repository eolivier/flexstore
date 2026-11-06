<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { JsonEnvironmentVariable } from '@/generated';
  import type { EnvironmentCategory } from '@/utils/environment';
  import EnvironmentVariableRow from './EnvironmentVariableRow.vue';

  // The variables of one category, in a table
  defineProps<{ category: EnvironmentCategory; variables: JsonEnvironmentVariable[] }>();
  const { t } = useI18n();
</script>

<template>
  <section class="mb-6">
    <h2 class="text-h6 font-weight-bold mb-2">{{ t(`environment.category.${category}`) }}</h2>
    <v-card>
      <v-table>
        <thead>
          <tr>
            <th>{{ t('environment.name') }}</th>
            <th>{{ t('environment.necessityTitle') }}</th>
            <th>{{ t('environment.statusTitle') }}</th>
            <th>{{ t('environment.value') }}</th>
          </tr>
        </thead>
        <tbody>
          <EnvironmentVariableRow v-for="variable in variables" :key="variable.name" :variable="variable" />
        </tbody>
      </v-table>
    </v-card>
  </section>
</template>
