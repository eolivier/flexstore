<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import NavLinkItems from './NavLinkItems.vue';
  import { accountLinksOf, adminLinks } from './navigation';
  import { useGuidedTourStore } from '@/stores/guidedTour.store';

  const props = defineProps<{ signedIn: boolean; admin: boolean; fullName: string; email: string }>();
  defineEmits<{ logout: [] }>();
  const { t } = useI18n();

  const accountLinks = computed(() => accountLinksOf(props.signedIn));

  // The guided tour keeps the menu open while it presents one of its entries
  const guidedTourStore = useGuidedTourStore();
  const isOpenedByTour = computed(() => guidedTourStore.presentedMenu === 'account');
  const isOpened = ref(false);
  // Under the spotlight of the tour (z-index 2300), so that the outline of the entry presented stays visible
  const menuZIndex = computed(() => (isOpenedByTour.value ? 2200 : undefined));
</script>

<template>
  <v-menu
    :model-value="isOpened || isOpenedByTour"
    :persistent="isOpenedByTour"
    :z-index="menuZIndex"
    location="bottom end"
    @update:model-value="isOpened = $event"
  >
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
