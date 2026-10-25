<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { PostalAddress } from '@/models/account';
  import { formatAddress } from '@/utils/address';

  // One address of the user, with the actions on it
  defineProps<{ address: PostalAddress }>();
  const emit = defineEmits<{ edit: []; remove: [] }>();

  const { t } = useI18n();
</script>

<template>
  <v-card class="pa-5 h-100 d-flex flex-column">
    <div class="d-flex align-start ga-3 mb-4">
      <v-icon
        :icon="address.recipientType === 'LEGAL_ENTITY' ? 'mdi-domain' : 'mdi-home-outline'"
        color="secondary"
      />
      <div class="text-body-1">{{ formatAddress(address) }}</div>
    </div>
    <v-spacer />
    <div class="d-flex ga-2">
      <v-btn variant="tonal" color="primary" prepend-icon="mdi-pencil-outline" @click="emit('edit')">
        {{ t('addressBook.edit') }}
      </v-btn>
      <v-btn variant="text" color="error" prepend-icon="mdi-delete-outline" @click="emit('remove')">
        {{ t('addressBook.remove') }}
      </v-btn>
    </div>
  </v-card>
</template>
