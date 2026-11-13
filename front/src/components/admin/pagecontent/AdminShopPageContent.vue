<script setup lang="ts">
  import LoadError from '@/components/layout/LoadError.vue';
  import AdminBreadcrumbs from '@/components/admin/AdminBreadcrumbs.vue';
  import { onMounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { shopPageContentTypes, useShopPageContentStore } from '@/stores/shopPageContent.store';
  import ShopPageContentForm from './ShopPageContentForm.vue';

  const { t } = useI18n();
  const shopPageContentStore = useShopPageContentStore();
  const loaded = ref(false);

  onMounted(async () => {
    await shopPageContentStore.fetchContents();
    loaded.value = true;
  });
</script>

<template>
  <v-container class="py-10">
    <AdminBreadcrumbs />
    <h1 class="text-h4 font-weight-bold mb-1">{{ t('shopPageContent.title') }}</h1>
    <p class="text-body-2 text-medium-emphasis mb-8">{{ t('shopPageContent.subtitle') }}</p>

    <LoadError v-if="shopPageContentStore.loadError" :message="t('shopPageContent.loadError')" />
    <v-skeleton-loader v-else-if="!loaded" type="paragraph" class="rounded-xl" />
    <template v-else>
      <v-card v-for="type in shopPageContentTypes" :key="type" class="pa-6 mb-6">
        <ShopPageContentForm :type="type" />
      </v-card>
    </template>
  </v-container>
</template>
