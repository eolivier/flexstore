<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { PostalAddress } from '@/models/account';
  import RecipientFields from '@/components/account/RecipientFields.vue';
  import PostalAddressFields from '@/components/account/PostalAddressFields.vue';

  // Edits a copy of an address: it is saved only when the user confirms
  const open = defineModel<boolean>({ required: true });
  const props = defineProps<{ address: PostalAddress; saving: boolean; error: string | null }>();
  const emit = defineEmits<{ save: [address: PostalAddress] }>();

  const { t } = useI18n();

  const editedAddress = ref<PostalAddress>({ ...props.address });
  const valid = ref<boolean | null>(null);

  watch(
    () => props.address,
    (address) => {
      editedAddress.value = { ...address };
    }
  );
</script>

<template>
  <v-dialog v-model="open" max-width="640">
    <v-card class="pa-6">
      <h2 class="text-h6 font-weight-bold mb-4">{{ t('addressBook.editTitle') }}</h2>
      <v-form v-model="valid" @submit.prevent="emit('save', editedAddress)">
        <RecipientFields v-model="editedAddress" purpose="delivery" />
        <PostalAddressFields v-model="editedAddress" />
        <v-alert v-if="error" type="error" variant="tonal" density="compact" class="mb-4">
          {{ t(`addressBook.errors.${error}`) }}
        </v-alert>
        <div class="d-flex justify-end ga-2">
          <v-btn variant="text" @click="open = false">{{ t('addressBook.cancel') }}</v-btn>
          <v-btn type="submit" color="secondary" :loading="saving" :disabled="valid === false">
            {{ t('addressBook.save') }}
          </v-btn>
        </div>
      </v-form>
    </v-card>
  </v-dialog>
</template>
