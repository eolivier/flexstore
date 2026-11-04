<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import DisableUserTwoFactorAuthenticationDialog from './DisableUserTwoFactorAuthenticationDialog.vue';

  const props = defineProps<{ userId: string; ownAccount?: boolean }>();
  const emit = defineEmits<{ disabled: [] }>();
  const { t } = useI18n();

  const dialogOpen = ref(false);
</script>

<template>
  <div class="mb-4">
    <p class="text-body-2 mb-1">
      <v-icon icon="mdi-shield-check-outline" size="small" class="mr-1" />{{ t('editUser.twoFactorEnabledText') }}
    </p>
    <p v-if="ownAccount" class="text-caption text-medium-emphasis">{{ t('editUser.twoFactorOwnAccountHint') }}</p>
    <v-btn v-else variant="outlined" color="error" size="small" @click="dialogOpen = true">{{
      t('editUser.twoFactorDisable')
    }}</v-btn>
    <DisableUserTwoFactorAuthenticationDialog
      v-model="dialogOpen"
      :user-id="props.userId"
      @disabled="emit('disabled')"
    />
  </div>
</template>
