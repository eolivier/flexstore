<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import FeedbackAlert from '@/components/layout/FeedbackAlert.vue';

  const { t } = useI18n();

  withDefaults(defineProps<{ message: string; retryable?: boolean }>(), { retryable: false });
  const emit = defineEmits<{ retry: [] }>();
</script>

<template>
  <FeedbackAlert type="error">
    {{ message }}
    <template v-if="retryable" #append>
      <v-btn variant="text" data-test="load-error-retry" @click="emit('retry')">{{ t('retry') }}</v-btn>
    </template>
  </FeedbackAlert>
</template>
