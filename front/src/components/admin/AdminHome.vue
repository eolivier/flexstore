<script setup lang="ts">
  import AdminBreadcrumbs from '@/components/admin/AdminBreadcrumbs.vue';
  import { useI18n } from 'vue-i18n';
  import { adminLinks } from '@/components/menu/navigation';

  const { t } = useI18n();

  const descriptions: Record<string, string> = {
    '/admin/users': 'admin.usersText',
    '/admin/create-user': 'admin.createUserText',
    '/admin/products': 'admin.productsText',
    '/admin/create-product': 'admin.createProductText',
    '/admin/orders': 'admin.ordersText',
    '/admin/settings': 'admin.settingsText',
  };
</script>

<template>
  <v-container class="py-10">
    <AdminBreadcrumbs />
    <h1 class="text-h4 font-weight-bold mb-1">{{ t('admin.title') }}</h1>
    <p class="text-body-2 text-medium-emphasis mb-8">{{ t('admin.subtitle') }}</p>

    <v-row>
      <v-col v-for="link in adminLinks" :key="link.to" cols="12" md="4">
        <v-card :to="link.to" class="pa-6 h-100">
          <v-avatar color="secondary" variant="tonal" rounded="lg" size="48" class="mb-4">
            <v-icon :icon="link.icon" />
          </v-avatar>
          <h2 class="text-h6 font-weight-bold mb-1">{{ t(link.label) }}</h2>
          <p class="text-body-2 text-medium-emphasis">{{ t(descriptions[link.to]) }}</p>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>
