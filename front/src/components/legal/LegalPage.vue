<script setup lang="ts">
  import { computed, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useLegalPagesStore, type LegalPageKind } from '@/stores/legalPages.store';

  const props = defineProps<{ kind: LegalPageKind }>();

  const { t, locale } = useI18n();
  const legalPagesStore = useLegalPagesStore();

  const legalPage = computed(() => legalPagesStore.legalPages[props.kind]);
  const isWritten = computed(() => (legalPage.value?.text.trim().length ?? 0) > 0);
  const publishedOn = computed(() =>
    legalPage.value
      ? new Date(legalPage.value.publishedAt).toLocaleDateString(locale.value, { dateStyle: 'long' })
      : ''
  );

  watch(
    () => props.kind,
    (kind) => legalPagesStore.fetchLegalPage(kind),
    { immediate: true }
  );
</script>

<template>
  <v-container class="legal-page py-12">
    <h1 class="text-h4 font-weight-bold mb-2">{{ t(`legalPages.${kind}.title`) }}</h1>

    <v-alert v-if="legalPagesStore.loadErrors[kind]" type="error" variant="tonal">{{
      t('legalPages.loadError')
    }}</v-alert>

    <v-skeleton-loader v-else-if="!legalPage" type="paragraph" />

    <template v-else-if="isWritten">
      <p class="text-body-2 text-medium-emphasis mb-8">
        {{ t('legalPages.publishedOn', { date: publishedOn }) }}
      </p>
      <div class="legal-text text-body-1">{{ legalPage.text }}</div>
    </template>

    <p v-else class="text-body-1 text-medium-emphasis">{{ t('legalPages.notWritten') }}</p>
  </v-container>
</template>

<style scoped>
  .legal-page {
    max-width: 760px;
  }
  .legal-text {
    white-space: pre-line;
    overflow-wrap: anywhere;
  }
</style>
