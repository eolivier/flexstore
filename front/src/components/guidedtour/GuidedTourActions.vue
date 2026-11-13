<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  // The buttons of the guided tour: ignore it, go back, go on (or finish on its last step)
  defineProps<{ isFirst: boolean; isLast: boolean }>();
  defineEmits<{ skip: []; previous: []; next: []; finish: [] }>();
  const { t } = useI18n();
</script>

<template>
  <v-card-actions>
    <v-btn variant="text" @click="$emit('skip')">{{ t('guidedTour.skip') }}</v-btn>
    <v-spacer />
    <v-btn v-if="!isFirst" variant="text" @click="$emit('previous')">{{ t('guidedTour.previous') }}</v-btn>
    <v-btn v-if="isLast" color="secondary" variant="flat" @click="$emit('finish')">
      {{ t('guidedTour.finish') }}
    </v-btn>
    <v-btn v-else color="secondary" variant="flat" @click="$emit('next')">{{ t('guidedTour.next') }}</v-btn>
  </v-card-actions>
</template>
