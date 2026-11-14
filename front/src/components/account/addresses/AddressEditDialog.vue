<script setup lang="ts">
  import FormActions from '@/components/form/FormActions.vue';
  import { computed, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { PostalAddress } from '@/models/account';
  import RecipientFields from '@/components/account/RecipientFields.vue';
  import PostalAddressFields from '@/components/account/PostalAddressFields.vue';
  import FeedbackAlert from '@/components/layout/FeedbackAlert.vue';

  // Adds a new address, or edits a copy of an existing one: it is saved only when the user confirms.
  // The address starts blank to add one, or as a copy of the edited one
  const open = defineModel<boolean>({ required: true });
  const props = defineProps<{
    address: PostalAddress;
    mode: 'add' | 'edit';
    saving: boolean;
    error: string | null;
  }>();
  const emit = defineEmits<{ save: [address: PostalAddress] }>();

  const { t } = useI18n();

  const title = computed(() => (props.mode === 'add' ? t('addressBook.addTitle') : t('addressBook.editTitle')));

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
      <h2 class="text-h6 font-weight-bold mb-4">{{ title }}</h2>
      <v-form v-model="valid" @submit.prevent="emit('save', editedAddress)">
        <RecipientFields v-model="editedAddress" purpose="delivery" />
        <PostalAddressFields v-model="editedAddress" />
        <FeedbackAlert v-if="error" type="error" compact class="mb-4">
          {{ t(`addressBook.errors.${error}`) }}
        </FeedbackAlert>
        <FormActions :loading="saving" :disabled="valid === false" @cancel="open = false" />
      </v-form>
    </v-card>
  </v-dialog>
</template>
