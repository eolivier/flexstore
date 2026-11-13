<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();

  withDefaults(
    defineProps<{
      disabled?: boolean;
      loading?: boolean;
      cancellable?: boolean;
      cancelDisabled?: boolean;
      cancelTo?: string;
    }>(),
    { disabled: false, loading: false, cancellable: true, cancelDisabled: false, cancelTo: undefined }
  );
  const emit = defineEmits<{ cancel: [] }>();
</script>

<template>
  <div class="d-flex align-center justify-end ga-2">
    <v-btn
      v-if="cancellable"
      variant="text"
      :to="cancelTo"
      :disabled="cancelDisabled || loading"
      data-test="form-cancel"
      @click="emit('cancel')"
    >
      {{ t('cancel') }}
    </v-btn>
    <v-btn type="submit" color="secondary" :loading="loading" :disabled="disabled" data-test="form-save">
      <slot>{{ t('save') }}</slot>
    </v-btn>
  </div>
</template>
