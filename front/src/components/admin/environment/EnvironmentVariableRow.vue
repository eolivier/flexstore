<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { JsonEnvironmentVariable } from '@/generated';
  import { isDefined, isRequiredButUndefined } from '@/utils/environment';

  // One variable: its name, whether it is required, whether it is defined and, for a plain variable, its value
  const props = defineProps<{ variable: JsonEnvironmentVariable }>();
  const { t } = useI18n();

  const statusColor = computed(() => {
    if (isDefined(props.variable)) return 'success';
    return isRequiredButUndefined(props.variable) ? 'error' : 'warning';
  });
</script>

<template>
  <tr>
    <td class="font-weight-medium text-no-wrap">{{ variable.name }}</td>
    <td>{{ t(`environment.necessity.${variable.necessity}`) }}</td>
    <td>
      <v-chip :color="statusColor" size="small" variant="tonal">
        {{ t(`environment.status.${variable.status}`) }}
      </v-chip>
    </td>
    <td class="text-medium-emphasis">
      <span v-if="variable.sensitivity === 'SECRET'">{{ t('environment.hidden') }}</span>
      <span v-else-if="variable.value">{{ variable.value }}</span>
    </td>
  </tr>
</template>
