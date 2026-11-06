<script setup lang="ts">
  import AdminBreadcrumbs from '@/components/admin/AdminBreadcrumbs.vue';
  import { onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useLegalPagesStore, type LegalPageKind } from '@/stores/legalPages.store';
  import LegalPageForm from './LegalPageForm.vue';

  const { t } = useI18n();
  const legalPagesStore = useLegalPagesStore();

  const kinds: LegalPageKind[] = ['legal-notice', 'terms-of-sale', 'privacy-policy'];

  onMounted(() => {
    kinds.forEach((kind) => legalPagesStore.fetchLegalPage(kind));
  });
</script>

<template>
  <v-container class="py-10">
    <AdminBreadcrumbs />
    <h1 class="text-h4 font-weight-bold mb-1">{{ t('legalPages.adminTitle') }}</h1>
    <p class="text-body-2 text-medium-emphasis mb-8">{{ t('legalPages.adminSubtitle') }}</p>

    <section v-for="kind in kinds" :key="kind" class="mb-8">
      <h2 class="text-h6 font-weight-bold mb-3">{{ t(`legalPages.${kind}.title`) }}</h2>
      <v-alert v-if="legalPagesStore.loadErrors[kind]" type="error" variant="tonal">{{
        t('legalPages.loadError')
      }}</v-alert>
      <v-skeleton-loader v-else-if="!legalPagesStore.legalPages[kind]" type="paragraph" class="rounded-xl" />
      <v-card v-else class="pa-6">
        <LegalPageForm :kind="kind" :legal-page="legalPagesStore.legalPages[kind]" />
      </v-card>
    </section>
    <p class="text-body-2 text-medium-emphasis">{{ t('legalPages.versionsNote') }}</p>
  </v-container>
</template>
