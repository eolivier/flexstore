<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import NavLinkItems from './NavLinkItems.vue';
  import { accountLinksOf, adminLinks } from './navigation';

  const props = defineProps<{ signedIn: boolean; admin: boolean; fullName: string; email: string }>();
  defineEmits<{ logout: [] }>();
  const { t } = useI18n();

  const accountLinks = computed(() => accountLinksOf(props.signedIn));
</script>

<template>
  <v-menu location="bottom end">
    <template #activator="{ props: activator }">
      <v-btn
        v-bind="activator"
        icon="mdi-account-circle-outline"
        variant="text"
        :aria-label="t('nav.account')"
        data-guided-tour="account"
      />
    </template>
    <v-list density="comfortable" min-width="240">
      <v-list-item v-if="signedIn" :subtitle="email">
        <v-list-item-title class="font-weight-bold">{{ fullName }}</v-list-item-title>
      </v-list-item>
      <v-divider v-if="signedIn" class="my-1" />
      <NavLinkItems :links="accountLinks" />
      <template v-if="admin">
        <v-divider class="my-1" />
        <v-list-subheader>{{ t('nav.admin') }}</v-list-subheader>
        <NavLinkItems :links="adminLinks" />
      </template>
      <v-divider v-if="signedIn" class="my-1" />
      <v-list-item v-if="signedIn" prepend-icon="mdi-logout" :title="t('nav.logout')" @click="$emit('logout')" />
      <template v-else>
        <v-list-item to="/login" prepend-icon="mdi-login" :title="t('nav.login')" />
        <v-list-item to="/sign-up" prepend-icon="mdi-account-plus-outline" :title="t('nav.signUp')" />
      </template>
    </v-list>
  </v-menu>
</template>
