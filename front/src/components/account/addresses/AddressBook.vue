<script setup lang="ts">
  import EmptyState from '@/components/layout/EmptyState.vue';
  import LoadError from '@/components/layout/LoadError.vue';
  import { computed, onMounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { blankPostalAddress, type PostalAddress } from '@/models/account';
  import { formatAddress } from '@/utils/address';
  import { useAddressBookStore } from '@/stores/addressBook.store';
  import AddressCard from './AddressCard.vue';
  import AddressEditDialog from './AddressEditDialog.vue';
  import ConfirmDialog from '@/components/ConfirmDialog.vue';
  import AccountBreadcrumbs from '@/components/account/AccountBreadcrumbs.vue';

  // The addresses of the signed-in user: they add, edit and remove them. The orders keep their own addresses
  const addressBookStore = useAddressBookStore();
  const { t } = useI18n();

  // The address being edited, or null while adding a new one
  const editedPosition = ref<number | null>(null);
  const mode = ref<'add' | 'edit'>('add');
  const editing = ref(false);
  const saving = ref(false);
  const editionError = ref<string | null>(null);
  const removalError = ref(false);
  // The position of the address the user is about to remove, once confirmed
  const positionToRemove = ref<number | null>(null);
  const removalMessage = computed(() => {
    if (positionToRemove.value === null) return '';
    const address = formatAddress(addressBookStore.addresses[positionToRemove.value]);
    return t('addressBook.deleteMessage', { address });
  });

  const editedAddress = computed(() =>
    mode.value === 'add' ? blankPostalAddress() : addressBookStore.addresses[editedPosition.value!]
  );

  onMounted(() => {
    addressBookStore.fetchAddresses();
  });

  function startAdding() {
    mode.value = 'add';
    editedPosition.value = null;
    editionError.value = null;
    editing.value = true;
  }

  function edit(position: number) {
    mode.value = 'edit';
    editedPosition.value = position;
    editionError.value = null;
    editing.value = true;
  }

  async function save(address: PostalAddress) {
    saving.value = true;
    const edition =
      mode.value === 'add'
        ? await addressBookStore.addAddress(address)
        : await addressBookStore.changeAddress(editedPosition.value!, address);
    saving.value = false;
    if (edition === 'saved') {
      editing.value = false;
    } else {
      editionError.value = edition;
    }
  }

  async function confirmRemoval() {
    if (positionToRemove.value === null) return;
    removalError.value = !(await addressBookStore.removeAddress(positionToRemove.value));
    positionToRemove.value = null;
  }
</script>

<template>
  <v-container class="py-10">
    <AccountBreadcrumbs />
    <div class="d-flex flex-wrap align-center justify-space-between ga-3">
      <div>
        <h1 class="text-h4 font-weight-bold">{{ t('addressBook.title') }}</h1>
        <p class="text-body-2 text-medium-emphasis mt-1">{{ t('addressBook.subtitle') }}</p>
      </div>
      <v-btn color="primary" variant="tonal" prepend-icon="mdi-plus" @click="startAdding">
        {{ t('addressBook.add') }}
      </v-btn>
    </div>

    <LoadError v-if="addressBookStore.loadError" :message="t('addressBook.loadError')" class="mt-6" />
    <v-alert v-if="removalError" type="error" variant="tonal" class="mt-6">
      {{ t('addressBook.errors.removal') }}
    </v-alert>

    <v-skeleton-loader v-if="addressBookStore.loading" type="card" class="rounded-xl mt-6" />
    <EmptyState
      v-else-if="addressBookStore.addresses.length === 0"
      icon="mdi-map-marker-off-outline"
      :message="t('addressBook.empty')"
      class="mt-6"
    >
      <v-btn color="primary" prepend-icon="mdi-plus" @click="startAdding">
        {{ t('addressBook.add') }}
      </v-btn>
    </EmptyState>
    <v-row v-else class="mt-1">
      <v-col v-for="(address, position) in addressBookStore.addresses" :key="position" cols="12" md="6">
        <AddressCard :address="address" @edit="edit(position)" @remove="positionToRemove = position" />
      </v-col>
    </v-row>

    <AddressEditDialog
      v-model="editing"
      :address="editedAddress"
      :mode="mode"
      :saving="saving"
      :error="editionError"
      @save="save"
    />

    <ConfirmDialog
      :model-value="positionToRemove !== null"
      :title="t('addressBook.deleteTitle')"
      :message="removalMessage"
      @update:model-value="(open: boolean) => !open && (positionToRemove = null)"
      @confirm="confirmRemoval"
    />
  </v-container>
</template>
