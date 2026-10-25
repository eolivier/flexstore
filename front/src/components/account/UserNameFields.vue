<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';

  // The first name and the last name of a user, side by side on a large screen: required unless said otherwise
  const firstName = defineModel<string>('firstName', { required: true });
  const lastName = defineModel<string>('lastName', { required: true });
  const props = withDefaults(defineProps<{ requirement?: 'required' | 'optional' }>(), {
    requirement: 'required',
  });

  const { t } = useI18n();

  const required = (v: string) => !!v?.trim() || t('createUser.mandatoryField');
  const rules = computed(() => (props.requirement === 'required' ? [required] : []));
</script>

<template>
  <v-row dense>
    <v-col cols="12" sm="6">
      <v-text-field
        v-model="firstName"
        :label="t('createUser.firstName')"
        :rules="rules"
        prepend-inner-icon="mdi-account-outline"
        autocomplete="given-name"
        class="mb-2"
        :required="requirement === 'required'"
      />
    </v-col>
    <v-col cols="12" sm="6">
      <v-text-field
        v-model="lastName"
        :label="t('createUser.lastName')"
        :rules="rules"
        autocomplete="family-name"
        class="mb-2"
        :required="requirement === 'required'"
      />
    </v-col>
  </v-row>
</template>
