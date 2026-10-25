<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { PostalAddress } from '@/models/account';
  import { useAddressBookStore } from '@/stores/addressBook.store';
  import AddressCard from './AddressCard.vue';
  import AddressEditDialog from './AddressEditDialog.vue';

  // The addresses of the signed-in user: they see, edit and remove them. The orders keep their own addresses
  const addressBookStore = useAddressBookStore();
  const { t } = useI18n();

  const editedPosition = ref<number | null>(null);
  const editing = ref(false);
  const saving = ref(false);
  const editionError = ref<string | null>(null);
  const removalError = ref(false);

  onMounted(() => {
    addressBookStore.fetchAddresses();
  });

  function edit(position: number) {
    editedPosition.value = position;
    editionError.value = null;
    editing.value = true;
  }

  async function save(address: PostalAddress) {
    saving.value = true;
    const edition = await addressBookStore.changeAddress(editedPosition.value!, address);
    saving.value = false;
    if (edition === 'saved') {
      editing.value = false;
    } else {
      editionError.value = edition;
    }
  }

  async function remove(position: number) {
    removalError.value = !(await addressBookStore.removeAddress(position));
  }
</script>

<template>
  <v-container class="py-10">
    <h1 class="text-h4 font-weight-bold">{{ t('addressBook.title') }}</h1>
    <p class="text-body-2 text-medium-emphasis mt-1 mb-6">{{ t('addressBook.subtitle') }}</p>

    <v-alert v-if="addressBookStore.loadError" type="error" variant="tonal" class="mb-4">
      {{ t('addressBook.loadError') }}
    </v-alert>
    <v-alert v-if="removalError" type="error" variant="tonal" class="mb-4">
      {{ t('addressBook.errors.removal') }}
    </v-alert>

    <v-skeleton-loader v-if="addressBookStore.loading" type="card" class="rounded-xl" />
    <v-card v-else-if="addressBookStore.addresses.length === 0" class="pa-10 text-center">
      <v-icon icon="mdi-map-marker-off-outline" size="48" class="text-medium-emphasis mb-3" />
      <p class="text-body-1">{{ t('addressBook.empty') }}</p>
    </v-card>
    <v-row v-else>
      <v-col
        v-for="(address, position) in addressBookStore.addresses"
        :key="position"
        cols="12"
        md="6"
      >
        <AddressCard :address="address" @edit="edit(position)" @remove="remove(position)" />
      </v-col>
    </v-row>

    <AddressEditDialog
      v-if="editedPosition !== null"
      v-model="editing"
      :address="addressBookStore.addresses[editedPosition]"
      :saving="saving"
      :error="editionError"
      @save="save"
    />
  </v-container>
</template>
