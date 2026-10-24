<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { AccountStatus } from '@/models/user';

  // The status of an account, chosen by an administrator; only the customer verifies their email
  const status = defineModel<AccountStatus>({ required: true });
  defineProps<{ disabled?: boolean }>();

  const { t } = useI18n();

  const icons: Record<AccountStatus, string> = {
    PENDING_VERIFICATION: 'mdi-email-alert-outline',
    ACTIVE: 'mdi-check-circle-outline',
    SUSPENDED: 'mdi-pause-circle-outline',
    LOCKED: 'mdi-lock-outline',
    DISABLED: 'mdi-cancel',
  };

  const statuses = computed(() =>
    (Object.keys(icons) as AccountStatus[]).map(value => ({
      title: t(`accountStatus.${value}`),
      value,
      props: { prependIcon: icons[value], disabled: value === 'PENDING_VERIFICATION' },
    }))
  );
</script>

<template>
  <v-select
    v-model="status"
    :items="statuses"
    :label="t('editUser.status')"
    :prepend-inner-icon="icons[status]"
    :disabled="disabled"
    :hint="disabled ? t('editUser.ownStatus') : t('editUser.statusHint')"
    persistent-hint
    class="mb-4"
  />
</template>
