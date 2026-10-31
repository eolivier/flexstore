<script setup lang="ts">
  import AdminBreadcrumbs from '@/components/admin/AdminBreadcrumbs.vue';
  import ConfirmDialog from '@/components/ConfirmDialog.vue';
  import { computed, onMounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { JsonDiscountCode } from '@/generated';
  import { useDiscountCodesStore } from '@/stores/discountCodes.store';
  import { formatPrice } from '@/utils/price';
  import NewDiscountCodeForm from './NewDiscountCodeForm.vue';

  // The discount codes: the administrator gives new ones, and deletes them
  const discountCodesStore = useDiscountCodesStore();
  const { t, locale } = useI18n();

  // The code to delete, once confirmed
  const codeToDelete = ref<string | null>(null);
  const deletionFailed = ref(false);

  onMounted(() => discountCodesStore.fetchDiscountCodes());

  const headers = computed(() => [
    { title: t('discountCodes.code'), key: 'code' },
    { title: t('discountCodes.reduction'), key: 'reduction', sortable: false },
    { title: t('discountCodes.expiresOn'), key: 'expiresAt' },
    { title: t('discountCodes.uses'), key: 'timesUsed', align: 'end' as const },
    { title: '', key: 'actions', sortable: false, align: 'end' as const },
  ]);

  function reductionOf(discountCode: JsonDiscountCode) {
    return discountCode.reductionType === 'PERCENTAGE'
      ? `${discountCode.reductionValue} %`
      : formatPrice(discountCode.reductionValue, '€', locale.value);
  }

  function expiryOf(discountCode: JsonDiscountCode) {
    return new Date(discountCode.expiresAt).toLocaleDateString(locale.value, { dateStyle: 'long' });
  }

  async function confirmDeletion() {
    if (!codeToDelete.value) return;
    deletionFailed.value = !(await discountCodesStore.deleteDiscountCode(codeToDelete.value));
    codeToDelete.value = null;
  }
</script>

<template>
  <v-container class="py-10">
    <AdminBreadcrumbs />
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('discountCodes.title') }}</h1>
    <NewDiscountCodeForm />
    <v-alert v-if="discountCodesStore.loadError" type="error" variant="tonal" class="mb-4">
      {{ t('discountCodes.loadError') }}
    </v-alert>
    <v-alert v-if="deletionFailed" type="error" variant="tonal" class="mb-4">
      {{ t('discountCodes.deletionError') }}
    </v-alert>
    <v-data-table
      :headers="headers"
      :items="discountCodesStore.discountCodes"
      :loading="discountCodesStore.loading"
      :no-data-text="t('discountCodes.none')"
      item-value="code"
    >
      <template #item.code="{ item }">
        <span class="font-weight-medium">{{ item.code }}</span>
      </template>
      <template #item.reduction="{ item }">{{ reductionOf(item) }}</template>
      <template #item.expiresAt="{ item }">{{ expiryOf(item) }}</template>
      <template #item.timesUsed="{ item }">{{ item.timesUsed }} / {{ item.usageLimit }}</template>
      <template #item.actions="{ item }">
        <v-btn
          icon="mdi-delete-outline"
          variant="text"
          size="small"
          color="error"
          :aria-label="t('discountCodes.delete', { code: item.code })"
          @click="codeToDelete = item.code"
        />
      </template>
    </v-data-table>
    <ConfirmDialog
      :model-value="codeToDelete !== null"
      :title="t('discountCodes.deleteTitle', { code: codeToDelete })"
      :message="t('discountCodes.deleteMessage')"
      @update:model-value="(open: boolean) => !open && (codeToDelete = null)"
      @confirm="confirmDeletion"
    />
  </v-container>
</template>
