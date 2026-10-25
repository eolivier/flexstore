<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRouter } from 'vue-router';
  import { useAuthStore } from '@/stores/auth.store';
  import ChangePasswordForm from '@/components/account/ChangePasswordForm.vue';
  import EmailVerificationBanner from '@/components/account/EmailVerificationBanner.vue';
  import { FullName } from '@/models/user';

  const { t } = useI18n();
  const router = useRouter();
  const authStore = useAuthStore();

  const initials = computed(() => FullName.of(authStore.currentUser).initials);

  const shortcuts = computed(() => [
    { to: '/orders', icon: 'mdi-truck-outline', label: 'nav.orders' },
    { to: '/cart', icon: 'mdi-cart-outline', label: 'nav.cart' },
    ...(authStore.isAdmin ? [{ to: '/admin', icon: 'mdi-cog-outline', label: 'nav.admin' }] : []),
  ]);

  function logout() {
    authStore.logout();
    router.push('/');
  }
</script>

<template>
  <v-container class="page py-10">
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('profile.title') }}</h1>

    <v-card v-if="!authStore.isAuthenticated" class="pa-10 text-center">
      <v-icon icon="mdi-account-circle-outline" size="56" class="text-medium-emphasis mb-3" />
      <h2 class="text-h6 font-weight-bold mb-1">{{ t('profile.notSignedIn') }}</h2>
      <p class="text-body-2 text-medium-emphasis mb-6">{{ t('profile.notSignedInText') }}</p>
      <v-btn
        :to="{ path: '/login', query: { redirect: '/user-profile' } }"
        color="primary"
        size="large"
        prepend-icon="mdi-login"
      >
        {{ t('nav.login') }}
      </v-btn>
    </v-card>

    <template v-else>
      <EmailVerificationBanner />
      <v-card class="pa-6 d-flex flex-column flex-sm-row align-sm-center ga-5 mb-6">
        <v-avatar color="secondary" size="72" class="text-h5 font-weight-bold">{{ initials }}</v-avatar>
        <div class="flex-grow-1">
          <div class="text-caption text-medium-emphasis">{{ t('profile.name') }}</div>
          <div class="text-h6 font-weight-bold mb-2">{{ FullName.of(authStore.currentUser).value }}</div>
          <div class="text-caption text-medium-emphasis">{{ t('profile.email') }}</div>
          <div class="text-body-1">{{ authStore.currentUser?.email }}</div>
        </div>
        <v-btn variant="outlined" color="primary" prepend-icon="mdi-logout" @click="logout">{{
          t('nav.logout')
        }}</v-btn>
      </v-card>

      <ChangePasswordForm class="mb-6" />

      <h2 class="text-h6 font-weight-bold mb-4">{{ t('profile.shortcuts') }}</h2>
      <v-row>
        <v-col v-for="shortcut in shortcuts" :key="shortcut.to" cols="12" sm="4">
          <v-card :to="shortcut.to" class="pa-5 d-flex align-center">
            <v-icon :icon="shortcut.icon" color="primary" class="mr-3" />
            <span class="text-subtitle-1 font-weight-medium">{{ t(shortcut.label) }}</span>
            <v-spacer />
            <v-icon icon="mdi-chevron-right" class="text-medium-emphasis" />
          </v-card>
        </v-col>
      </v-row>
    </template>
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 860px;
  }
</style>
