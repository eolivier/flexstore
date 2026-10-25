<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { User } from '@/models/user';

  type Role = NonNullable<User['role']>;

  const role = defineModel<Role>({ required: true });

  const { t } = useI18n();

  const roles: { value: Role; icon: string }[] = [
    { value: 'USER', icon: 'mdi-account-outline' },
    { value: 'ADMIN', icon: 'mdi-shield-account-outline' },
  ];
</script>

<template>
  <fieldset class="role-field mb-4">
    <legend class="text-subtitle-2 font-weight-bold mb-2">{{ t('createUser.role') }}</legend>
    <v-radio-group v-model="role" hide-details>
      <v-radio v-for="choice in roles" :key="choice.value" :value="choice.value" color="secondary">
        <template #label>
          <div class="d-flex align-center ga-2 py-1">
            <v-icon :icon="choice.icon" size="20" />
            <span>
              <span class="font-weight-medium">{{ t(`createUser.roles.${choice.value}.label`) }}</span>
              <span class="d-block text-caption text-medium-emphasis">{{
                t(`createUser.roles.${choice.value}.text`)
              }}</span>
            </span>
          </div>
        </template>
      </v-radio>
    </v-radio-group>
  </fieldset>
</template>

<style scoped>
  .role-field {
    border: 0;
    padding: 0;
    margin: 0;
  }
</style>
